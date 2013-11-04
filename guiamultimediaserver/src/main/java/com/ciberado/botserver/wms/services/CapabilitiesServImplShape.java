package com.ciberado.botserver.wms.services;

import com.ciberado.lang.SystemException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Document;
import org.jdom.Element;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Implementa el servicio de GetCapabilities. */
public class CapabilitiesServImplShape implements CapabilitiesServ {

    /** Indica d?nde se encuentra el fichero xml que sirve complantilla. */
    static final String TEMPLATE_PATH = "META-INF/basicgetcapabilities.xml";
    /** Documento generado con las capabilities. */
    protected Document capDocument = null;
    protected Namespace rootNS = null;
    /** DAO de acceso a datos para recuperar la lista de taxons. */
    private String address;
    private String name;
    private String title;
    private String abstrazt;
    private String organization;
    private Map<String, FeatureSource> featureSources;

    public CapabilitiesServImplShape() {
        super();
    }

    /**
     * A?ade un nuevo elemento Layer al documento.
     * 
     * @param rootLayer
     *            Element al que sea a?ade.
     * @param name
     *            Name del layer (opcional).
     * @param title
     *            Title del layer.
     * @return
     */
    protected Element appendLayer(Element rootLayer, String name, String title) {
        Element newLayer = null;

        if (title != null) {
            newLayer = new Element("Layer", rootNS);
            rootLayer.addContent(newLayer);
            newLayer.setAttribute("queryable", "1");
            if (name != null) {
                Element nameElement = new Element("Name", rootNS);
                newLayer.addContent(nameElement);
                nameElement.addContent(new Text(name));
            }
            Element titleElement = new Element("Title", rootNS);
            newLayer.addContent(titleElement);
            titleElement.addContent(new Text(title));
        } else {
            newLayer = rootLayer;
        }

        return newLayer;
    }
    private static Map<FeatureSource, String> crsMapCache;

    private Map<FeatureSource, String> getCRSMap() {

        if (crsMapCache == null) {
            crsMapCache = new HashMap<FeatureSource, String>();

            for (FeatureSource source : this.featureSources.values()) {
                try {
                    CoordinateReferenceSystem crs = source.getInfo().getCRS();
                    if (crs == null) {
                        crs = DefaultGeographicCRS.WGS84;
                    }
                    String code = CRS.lookupIdentifier(Citations.EPSG, crs, true);
                    crsMapCache.put(source, code);
                } catch (FactoryException ex) {
                    Logger.getLogger(CapabilitiesServImplShape.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return crsMapCache;
    }

    private double[] getReferenceBoundaries() {
        double minx = Double.MAX_VALUE;
        double maxx = Double.MIN_VALUE;
        double miny = Double.MAX_VALUE;
        double maxy = Double.MIN_VALUE;

        try {
            for (FeatureSource source : this.featureSources.values()) {
                CoordinateReferenceSystem sourceCrs = source.getInfo().getCRS();
                if (sourceCrs == null) {
                    sourceCrs = DefaultGeographicCRS.WGS84;
                }
                MathTransform transform = CRS.findMathTransform(sourceCrs, DefaultGeographicCRS.WGS84, true);
                DirectPosition point = source.getBounds().getLowerCorner();
                point = transform.transform(point, point);
                double lon = point.getOrdinate(0);
                double lat = point.getOrdinate(1);
                minx = Math.min(lon, minx);
                miny = Math.min(lat, miny);
                point = source.getBounds().getUpperCorner();
                point = transform.transform(point, point);
                lon = point.getOrdinate(0);
                lat = point.getOrdinate(1);
                maxx = Math.max(lon, maxx);
                maxy = Math.max(lat, maxy);
            }

            return new double[]{minx, maxx, miny, maxy};
        } catch (FactoryException ex) {
            throw new SystemException(ex);
        } catch (IOException ex) {
            throw new SystemException(ex);
        } catch (TransformException ex) {
            throw new SystemException(ex);
        }

    }

    private String updateCapabilitiesTemplate(String capabilitiesTemplate) throws IOException {
        if ((capabilitiesTemplate.indexOf("#address#") != -1) && (address != null)) {
            //String address = httpRequest.getRequestURL().toString();
            capabilitiesTemplate = capabilitiesTemplate.replaceAll("#address#", address);
        }
        if ((capabilitiesTemplate.contains("#name#") == true) && (name != null)) {
            capabilitiesTemplate = capabilitiesTemplate.replaceAll("#name#", name);
        }
        if ((capabilitiesTemplate.contains("#title#") == true)) {
            capabilitiesTemplate = capabilitiesTemplate.replaceAll("#title#", title);
        }
        if ((capabilitiesTemplate.contains("#abstract#") == true) && (abstrazt != null)) {
            capabilitiesTemplate = capabilitiesTemplate.replaceAll("#abstract#", abstrazt);
        }
        if ((capabilitiesTemplate.contains("#organization#") == true) && (organization != null)) {
            capabilitiesTemplate = capabilitiesTemplate.replaceAll("#organization#", organization);
        }
        if (capabilitiesTemplate.indexOf("#CRS#") != -1) {
            StringBuilder chunk = new StringBuilder();
            for (String crs : getCRSMap().values()) {
                chunk.append("<CRS>" + crs + "</CRS>");
            }
            double[] bounds = getReferenceBoundaries();
            double minx = bounds[0];
            double maxx = bounds[1];
            double miny = bounds[2];
            double maxy = bounds[3];
            chunk.append(
                    "<EX_GeographicBoundingBox> "
                    + "        <westBoundLongitude> "
                    + minx
                    + "        </westBoundLongitude> "
                    + "        <eastBoundLongitude> "
                    + maxx
                    + "        </eastBoundLongitude> "
                    + "        <southBoundLatitude> "
                    + miny
                    + "        </southBoundLatitude> "
                    + "        <northBoundLatitude> "
                    + maxy
                    + "        </northBoundLatitude> "
                    + "</EX_GeographicBoundingBox> ");
            chunk.append(
                    "<BoundingBox CRS=\"EPSG:4326\" "
                    + "        minx=\"" + minx + "\" miny=\"" + miny + "\" "
                    + "        maxx=\"" + maxx + "\" maxy=\"" + maxy + "\" /> ");
            for (Map.Entry<FeatureSource, String> entry : getCRSMap().entrySet()) {
                ReferencedEnvelope envelope = entry.getKey().getBounds();
                chunk.append(
                        "<BoundingBox CRS=\"" + entry.getValue() + "\" "
                        + "        minx=\"" + envelope.getMinX() + "\" miny=\"" + envelope.getMinY() + "\" "
                        + "        maxx=\"" + envelope.getMaxX() + "\" maxy=\"" + envelope.getMaxY() + "\" /> ");
            }

            capabilitiesTemplate = capabilitiesTemplate.replace("#CRS#", chunk);
        }

        return capabilitiesTemplate;
    }

    /**
     * Carga la plantilla, a?ade los tipos de formato de imagen soportados y
     * utiliza el dao para agregar todos los taxons como layers. Por cada taxon
     * se invocar? visitLayer que se redefine en las subclase con la t?ctica que
     * se desee utilizar.
     * 
     * @return El documento con las capabilities.
     * 
     * @throws IOException
     * @throws SAXException
     */
    public Document getCapabilities() throws IOException {
        if (capDocument == null) {
            BufferedReader templateReader = null;

            try {
                templateReader = new BufferedReader(
                        new InputStreamReader(
                        CapabilitiesServImplShape.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH)));
                StringBuilder templateBuffer = new StringBuilder();
                String line = null;
                do {
                    line = templateReader.readLine();
                    if (line != null) {
                        templateBuffer.append(line).append("\r\n");
                    }
                } while (line != null);
                String template = this.updateCapabilitiesTemplate(templateBuffer.toString());

                StringReader templateStringReader = new StringReader(template);
                InputSource inputSource = new InputSource(templateStringReader);
                SAXBuilder builder = new SAXBuilder();
                capDocument = builder.build(inputSource);
                rootNS = capDocument.getRootElement().getNamespace();

                // Agregamos la lista de formatos extra soportados (image/gif
                // siempre est? disponible).
                Element getMapElement = capDocument.getRootElement().getChild("Capability", rootNS).getChild("Request", rootNS).getChild("GetMap", rootNS);
                String[] formatNames = {"image/png", "image/jpeg", "image/jpg"};
                for (int idx = 0; idx < formatNames.length; idx++) {
                    Element formatElement = new Element("Format", rootNS);
                    formatElement.addContent(new Text(formatNames[idx]));
                    getMapElement.addContent(0, formatElement);
                }

                Element rootLayer = capDocument.getRootElement().getChild("Capability", rootNS).getChild("Layer", rootNS);
                appendLayer(rootLayer, "Fitoepisodis", "Fitoepisodis");
                appendLayer(rootLayer, "Punts", "Punts");
                appendLayer(rootLayer, "Camins", "Camins");
            } catch (JDOMException e) {
                throw new SystemException(e);
            } finally {
                if (templateReader != null) {
                    try {
                        templateReader.close();
                    } catch (IOException e) {
                        throw new SystemException(e);
                    }
                }
            }
        }

        return capDocument;
    }

    public String getAbstract() {
        return abstrazt;
    }

    public void setAbstract(String abstrazt) {
        this.abstrazt = abstrazt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Document getCapDocument() {
        return capDocument;
    }

    public void setCapDocument(Document capDocument) {
        this.capDocument = capDocument;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFeatureSources(Map<String, FeatureSource> featureSources) {
        this.featureSources = featureSources;
    }
}
