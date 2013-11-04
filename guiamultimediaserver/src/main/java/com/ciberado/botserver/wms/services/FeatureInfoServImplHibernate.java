/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.model.Specimen;
import com.ciberado.botserver.wms.WMSGetFeatureInfoRequest;
import com.ciberado.lang.SystemException;
import java.awt.Rectangle;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 *
 * http://localhost:8084/botserver/wms?REQUEST=GetFeatureInfo&I=277&J=87&SERVICE=WMS&VERSION=1.1.1&LAYERS=fitoepisodis&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1562447823404787,41.359944084302526,2.162553062283644,41.36531997771978&WIDTH=512&HEIGHT=512&REASPECT=false&querylayers=0
 *
 * @author ciberado
 */
public class FeatureInfoServImplHibernate implements FeatureInfoServ {
    private double MAX_FEATURE_DISTANCE_TO_CENTER = 5.0; //meters

    private static final Log log = LogFactory.getLog(FeatureInfoServImplHibernate.class);
    protected static final int MAX_FEATURES = 10;
    
    @PersistenceContext 
    private EntityManager em;
    
    @Inject
    private ObjectMapper mapper;
    
    public FeatureInfoServImplHibernate() {
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

    //http://127.0.0.1:8084/botserver/wms?REQUEST=GetFeatureInfo&SERVICE=WMS&QUERY=Euph,2.15,42.11'&VERSION=1.1.1&LAYERS=pointsOfInterest&query_layers=pointsOfInterest&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1550958557555266,41.359636629839656,2.161404296976646,41.365012722558106&WIDTH=512&HEIGHT=512&REASPECT=false&i=250&j=250
    /**
     *
     * Retorna los puntos de inters situados cerca de la coordenada indicada.
     * 
     * La consulta incluir el parmetro query dividido por comas con los valores del
     * prefijo a buscar, la latitud del punto central y la longitud del mismo.
     *
     * @param wmsRequest
     * @param out
     */
    @Override
    public void processFeatureSearch(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out) {
        try {
            String[] queryParts = wmsRequest.getQuery().split(",");
            String prefix = "%" + queryParts[0].toLowerCase().replaceAll(" ", "%") + "%";
            double centerLat = Double.parseDouble(queryParts[1]);
            double centerLon = Double.parseDouble(queryParts[2]);
            
            String jpql =  "select s from Specimen s join fetch s.taxon  "
                         + "where s.keywords like :value "
                         + "order by (s.lat - :centerLat)*(s.lat - :centerLat) + "
                         + "         (s.lon - :centerLon)*(s.lon - :centerLon)";
            List<Specimen> specimens = em.createQuery(jpql)
                                         .setParameter("value", prefix)
                                         .setParameter("centerLat", centerLat)
                                         .setParameter("centerLon", centerLon)
                                         .setMaxResults(MAX_FEATURES)
                                         .getResultList();
            mapper.writeValue(out, specimens);            
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new SystemException(ex);
        }
    }

    /*
        [ {
          "code" : 2027,
          "lat" : 41.36226291397048,
          "lon" : 2.159967309500731,
          "taxon" : {
            "code" : "Tilpla",
            "family" : "Malvaceae",
            "genus" : "Tilia",
            "speciesName" : "platyphyllos",
            "speciesAuthor" : null,
            "subspecies" : null,
            "subspeciesAuthor" : null,
            "worldLocation" : "Europa, SW �sia",
            "popularNameCA" : "Tell de fulla gran",
            "popularNameES" : "Tilo de hoja grande",
            "popularNameEN" : "Broad-leaved lime",
            "descriptions" : {
              "ca" : "Especie bastante exigente en humedad, puede habitar en suelos calizos o sil�ceos. Llega a formar rodales monoespec�ficos. Bosques mixtos de laderas rocosas, hoces, barrancos. Crece salpicado en los bosques caducifolios donde suele aparecer en compañia de quejigos, pinos, avellanos, hayas, arces, serbales y fresnos."
            },
            "media" : {
              "flower" : "",
              "leaf" : "",
              "scanner" : "",
              "drawn" : "nt"
            }
          }
        }]
      
     
     */
    @Override
    @Transactional
    public void processGetFeatureInfo(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out)
            throws IOException, NoninvertibleTransformException {
        double minLon = wmsRequest.getMapRequest().getBox().getMinx();
        double maxLon = wmsRequest.getMapRequest().getBox().getMaxx();
        double minLat = wmsRequest.getMapRequest().getBox().getMiny();
        double maxLat = wmsRequest.getMapRequest().getBox().getMaxy();
        
        Point2D worldPoint;
        if ((minLon == maxLon) && (minLat == maxLat)) {
            worldPoint = new Point2D.Double(minLon, minLat);
        } else {
            int i, j;
            if ((wmsRequest.getI() == -1) && (wmsRequest.getJ() == -1)) {
                i = wmsRequest.getMapRequest().getWidth() / 2;
                j = wmsRequest.getMapRequest().getHeight() / 2;
            } else {
                i = wmsRequest.getI();
                j = wmsRequest.getJ();
            }
            ReferencedEnvelope activeArea = new ReferencedEnvelope(
                    minLon, maxLon, minLat, maxLat, DefaultGeographicCRS.WGS84);
            worldPoint = this.fromScreenToWorldPoint(
                    wmsRequest.getMapRequest().getWidth(), wmsRequest.getMapRequest().getHeight(),
                    activeArea, i, j);
        }
        
        log.debug("Point: " + wmsRequest.getI() + ", " + wmsRequest.getJ() + " -> " + worldPoint);
        
        GeodeticCalculator gc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
        gc.setStartingGeographicPoint(worldPoint.getX(), worldPoint.getY());
        gc.setDirection(-45, MAX_FEATURE_DISTANCE_TO_CENTER);
        Point2D topLeft = gc.getDestinationGeographicPoint();
        gc.setDirection(+45+90, MAX_FEATURE_DISTANCE_TO_CENTER);
        Point2D bottomRight = gc.getDestinationGeographicPoint();
        
        String jpql = "select s from Specimen as s left join fetch s.taxon "
                    + "where s.lat between :minLat and :maxLat " 
                    + "  and s.lon between :minLon and :maxLon "
                    + "order by (s.lat - :centerLat)*(s.lat - :centerLat) + "
                    + "         (s.lon - :centerLon)*(s.lon - :centerLon)";
        List<Specimen> specimens = em.createQuery(jpql)
                                     .setParameter("minLon", topLeft.getX())
                                     .setParameter("maxLon", bottomRight.getX())
                                     .setParameter("minLat", bottomRight.getY())
                                     .setParameter("maxLat", topLeft.getY())
                                     .setParameter("centerLat", worldPoint.getY())
                                     .setParameter("centerLon", worldPoint.getX())
                                     .setMaxResults(MAX_FEATURES)
                                     .getResultList();            
        mapper.writeValue(out, specimens);            
    }


}
