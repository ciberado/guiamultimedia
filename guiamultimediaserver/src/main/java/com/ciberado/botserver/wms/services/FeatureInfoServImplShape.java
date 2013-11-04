/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.datapublishing.InfoCard;
import com.ciberado.botserver.wms.WMSGetFeatureInfoRequest;
import com.ciberado.botserver.wms.WMSGetMapRequestLayer;
import com.ciberado.lang.SystemException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.PrintWriter; 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.opengis.filter.Filter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

/**
 *
 *
 * http://localhost:8084/botserver/wms?REQUEST=GetFeatureInfo&I=277&J=87&SERVICE=WMS&VERSION=1.1.1&LAYERS=fitoepisodis&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1562447823404787,41.359944084302526,2.162553062283644,41.36531997771978&WIDTH=512&HEIGHT=512&REASPECT=false&querylayers=0
 *
 * @author ciberado
 */
public class FeatureInfoServImplShape implements FeatureInfoServ {
    private double MAX_FEATURE_DISTANCE_TO_CENTER = 5.0;

    private static final Log log = LogFactory.getLog(FeatureInfoServImplShape.class);
    protected static final int MAX_FEATURES = 60;
    @Inject
    private ObjectMapper mapper;
    private Map<String, FeatureSource> featureSources;
    @Inject
    InfoCardService infoCardService;
    /** Name of the field in the featureSource that must have a value of 1
     *  in order to allow that feature to be considered in the selection.
     *  Set it to null to work with the full set of features.
     */
    private String featureSourceFilterFieldName;
    /** Name of the field used to do a search */
    private String searchIdField;
    /** Name of the key field in the featureSource */
    private String primaryIdField;

    public FeatureInfoServImplShape() {
    }

    private Point2D fromScreenToWorldPoint(
            int imageWidth, int imageHeight,
            ReferencedEnvelope activeArea,
            int x, int y)
            throws NoninvertibleTransformException {
        Rectangle rectangle = new Rectangle(0, 0, imageWidth, imageHeight);
        Point2D pointOrigin = new Point2D.Double(x, y);
        Point2D pointDest = new Point2D.Double();
        RendererUtilities.worldToScreenTransform(activeArea, rectangle).inverseTransform(pointOrigin, pointDest);


        return pointDest;
    }

    private AffineTransform fromScreenToWorldTransform(
            int imageWidth, int imageHeight,
            ReferencedEnvelope activeArea)
            throws NoninvertibleTransformException {
        Rectangle rectangle = new Rectangle(0, 0, imageWidth, imageHeight);
        AffineTransform transform =
                RendererUtilities.worldToScreenTransform(activeArea, rectangle);
        transform.invert();

        return transform;
    }

    //http://127.0.0.1:8084/botserver/wms?REQUEST=GetFeatureInfo&SERVICE=WMS&QUERY=Euph,2.15,42.11'&VERSION=1.1.1&LAYERS=pointsOfInterest&query_layers=pointsOfInterest&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1550958557555266,41.359636629839656,2.161404296976646,41.365012722558106&WIDTH=512&HEIGHT=512&REASPECT=false&i=250&j=250
    /**
     *
     * Retorna los puntos de inter�s situados cerca de la coordenada indicada.
     * 
     * La consulta incluir� el par�metro query dividido por comas con los valores del
     * prefijo a buscar, la latitud del punto central y la longitud del mismo.
     *
     * @param wmsRequest
     * @param out
     */
    @Override
    public void processFeatureSearch(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out) {
        try {
            String layerName = wmsRequest.getQueryLayers()[0].getName();
            FeatureSource source = this.featureSources.get(layerName);
            String[] queryParts = wmsRequest.getQuery().split(",");
            String query = MessageFormat.format("{0} like ''{1}%''",
                    this.searchIdField, queryParts[0]);
            double x = Double.parseDouble(queryParts[2]);
            double y = Double.parseDouble(queryParts[1]);
            Filter filter = CQL.toFilter(query);
            FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = source.getFeatures(filter);
            Point2D worldPoint = new Point2D.Double(x, y);
            List<InfoCard> infoCardList = this.featuresToInfoCards(worldPoint, -1, selectedFeatures);
            String jsonString = mapper.writeValueAsString(infoCardList);
            out.write(jsonString);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new SystemException(ex);
        } catch (CQLException ex) {
            ex.printStackTrace();
            throw new SystemException(ex);
        }
    }

    @Override
    public void processGetFeatureInfo(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out)
            throws IOException, NoninvertibleTransformException {
        List<InfoCard> infoCardList = new ArrayList<InfoCard>();
        for (WMSGetMapRequestLayer layer : wmsRequest.getQueryLayers()) {
            FeatureSource source = this.featureSources.get(layer.getName());
            if (source == null) {
                continue;
            }

            CoordinateReferenceSystem crs = source.getInfo().getCRS();
            if (crs == null) {
                crs = DefaultGeographicCRS.WGS84;
            }
            ReferencedEnvelope activeArea = new ReferencedEnvelope(
                    wmsRequest.getMapRequest().getBox().getMinx(),
                    wmsRequest.getMapRequest().getBox().getMaxx(),
                    wmsRequest.getMapRequest().getBox().getMiny(),
                    wmsRequest.getMapRequest().getBox().getMaxy(), crs);
            Rectangle screenRect;
            int i, j;
            double maxDistanceLimit;
            if ((wmsRequest.getI() == -1) && (wmsRequest.getJ() == -1)) {
                screenRect = new Rectangle(0, 0, wmsRequest.getMapRequest().getWidth(), wmsRequest.getMapRequest().getHeight());
                i = wmsRequest.getMapRequest().getWidth() / 2;
                j = wmsRequest.getMapRequest().getHeight() / 2;
                maxDistanceLimit = -1;
            } else {
                screenRect = new Rectangle(wmsRequest.getI() - 20, wmsRequest.getJ() - 20, 40, 40);
                i = wmsRequest.getI();
                j = wmsRequest.getJ();
                maxDistanceLimit = MAX_FEATURE_DISTANCE_TO_CENTER;
            }
            Point2D worldPoint = this.fromScreenToWorldPoint(
                    wmsRequest.getMapRequest().getWidth(), wmsRequest.getMapRequest().getHeight(),
                    activeArea, i, j);
            log.debug("Point: " + wmsRequest.getI() + ", " + wmsRequest.getJ() + " -> " + worldPoint);
            Rectangle2D worldRect = fromScreenToWorldTransform(
                    wmsRequest.getMapRequest().getWidth(), wmsRequest.getMapRequest().getHeight(), activeArea).createTransformedShape(screenRect).getBounds2D();
            ReferencedEnvelope bbox = new ReferencedEnvelope(worldRect, crs);
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            GeometryDescriptor geomDesc = source.getSchema().getGeometryDescriptor();
            String geometryAttributeName = geomDesc.getLocalName();
            Filter filter;
            if (featureSourceFilterFieldName == null) {
                filter = ff.bbox(ff.property(geometryAttributeName), bbox);
            } else {
                filter =
                        ff.and(
                        ff.equal(ff.property(featureSourceFilterFieldName), ff.literal(1), true),
                        ff.bbox(ff.property(geometryAttributeName), bbox));
            }

            FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures =
                    source.getFeatures(filter);
            infoCardList.addAll(this.featuresToInfoCards(worldPoint, maxDistanceLimit, selectedFeatures));
        }
        String jsonString = mapper.writeValueAsString(infoCardList);
        out.write(jsonString);
    }

    class SimpleFeatureCompartor implements Comparator<SimpleFeature> {

        private Coordinate coordCenter;
        private CoordinateReferenceSystem crs;

        public SimpleFeatureCompartor(Coordinate coordCenter, CoordinateReferenceSystem crs) {
            this.coordCenter = coordCenter;
            this.crs = crs;
        }

        private double distance(Point2D from, Point to) {
            return Math.sqrt(
                    Math.pow(to.getX() - from.getX(), 2)
                    + Math.pow(to.getY() - from.getY(), 2));
        }

        public int compare(SimpleFeature f1, SimpleFeature f2) {
            try {
                Coordinate coordFeature1 = new Coordinate(
                    f1.getDefaultGeometryProperty().getBounds().getMedian(0),
                    f1.getDefaultGeometryProperty().getBounds().getMedian(1));
                Coordinate coordFeature2 = new Coordinate(
                    f2.getDefaultGeometryProperty().getBounds().getMedian(0),
                    f2.getDefaultGeometryProperty().getBounds().getMedian(1));
                
               double distance1 = Math.abs(JTS.orthodromicDistance(coordCenter, coordFeature1, crs));
               double distance2 = Math.abs(JTS.orthodromicDistance(coordCenter, coordFeature2, crs));
               
               int result = 0;
               if (distance1 < distance2) {
                   result = -1;
               } else {
                   result = +1;
               }
               return result;
            } catch (TransformException ex) {
                ex.printStackTrace();
                throw new SystemException(ex);
            }
        }
    }

    private List<InfoCard> featuresToInfoCards(
            Point2D center, double maxDistanceLimit,
            FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
        FeatureIterator<SimpleFeature> iter = null;
        CoordinateReferenceSystem crs = null;
        try {
            Coordinate coordCenter = new Coordinate(center.getX(), center.getY());
            iter = features.features();
            List<SimpleFeature> featureList = new ArrayList<SimpleFeature>();
            while (iter.hasNext() == true) {
                SimpleFeature feature = iter.next();
                if (crs == null) {
                    crs = feature.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
                    if (crs == null) {
                        crs = DefaultGeographicCRS.WGS84;
                    }
                }
                if (maxDistanceLimit != -1) {
                    Coordinate coordFeature = new Coordinate(
                            feature.getDefaultGeometryProperty().getBounds().getMedian(0),
                            feature.getDefaultGeometryProperty().getBounds().getMedian(1));
                    double distance = JTS.orthodromicDistance(coordCenter, coordFeature, crs);
                    if (distance <= maxDistanceLimit) {
                        featureList.add(feature);
                    }
                } else {
                    featureList.add(feature);
                }
            }
            if (center != null) {
                SimpleFeatureCompartor comparator = new SimpleFeatureCompartor(coordCenter, crs);
                Collections.sort(featureList, comparator);
            }
            featureList = featureList.subList(0, Math.min(featureList.size(), MAX_FEATURES));
            List<InfoCard> infoCardList = new ArrayList<InfoCard>();
            for (int idxFeature = 0; idxFeature < featureList.size(); idxFeature++) {
                SimpleFeature feature = featureList.get(idxFeature);
                Object idValue = feature.getAttribute(this.primaryIdField);
                if (idValue != null) {
                    InfoCard infoCard = infoCardService.getInfoCard(String.valueOf(idValue));
                    infoCardList.add(infoCard);
                }
            }
            return infoCardList;
        } catch (TransformException ex) {
            ex.printStackTrace();
            throw new SystemException(ex);
        } finally {
            iter.close();
        }
    }

    public void setFeatureSources(Map<String, FeatureSource> featureSources) {
        this.featureSources = featureSources;
    }

    public void setPrimaryIdField(String primaryIdField) {
        this.primaryIdField = primaryIdField;
    }

    public void setSearchIdField(String searchIdField) {
        this.searchIdField = searchIdField;
    }

    public void setFeatureSourceFilterFieldName(String featureSourceFilterFieldName) {
        this.featureSourceFilterFieldName = featureSourceFilterFieldName;
    }
}
