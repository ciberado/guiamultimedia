package com.ciberado.botserver.wms;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/** Permite analizar las peticiones http y encapsularlas en peticiones WMS. 
 *  
 */
public class WMSRequest {

    /** Versin (nosotros soportamos 1.3.0). */
    private String version;
    /** Servicio requerido (GetMap, GetCapabilities, GetFeatureInfo). */
    private String request;
    /** Formato esperado de la respuesta (text/xml o text/html). */
    private String format;
    /** Propietario: indica si se desea utilizar la versin de la cach. */
    private boolean cacheable;
    /** Propietario: la coordenada horizontal del tile requerido. */
    private int tileX;
    /** Propietario: la coordenada vertical del tile requerido. */
    private int tileY;
    /** Propietario: nivel del zoom de la rejilla de tiles. */
    private int tileZoom;

    public WMSRequest() {
        this(null, null, null);
    }

    public WMSRequest(String version, String request, String format) {
        super();
        this.setVersion(version);
        this.request = request;
        this.setFormat(format);
    }

    public static WMSRequest getInstance(HttpServletRequest httpRequest)
            throws IllegalArgumentException {
        WMSRequest wmsRequest;

        // Los nombres de los parametros no son casesensitive por lo que los
        // pasamos a mayusculas en una hash alternativa.
        Map<String, String> upperCaseParams = new HashMap<String, String>();
        Enumeration<String> names = httpRequest.getParameterNames();
        while (names.hasMoreElements() == true) {
            String currentName = names.nextElement();
            upperCaseParams.put(currentName.toUpperCase(), httpRequest.getParameter(currentName));
        }
        String requestParam = upperCaseParams.get("REQUEST");
        if (requestParam == null) {
            throw new IllegalArgumentException("REQUEST param mandatory.");
        }
        String crsParam = upperCaseParams.get("CRS") != null
                ? upperCaseParams.get("CRS")
                : upperCaseParams.get("SRS");
        if (requestParam.equals("GetCapabilities") == true) {
            wmsRequest = new WMSGetCapabilitiesRequest();
            wmsRequest.setVersion(upperCaseParams.get("VERSION"));
            wmsRequest.setFormat(upperCaseParams.get("FORMAT"));

        } else if (requestParam.equals("GetMap") == true) {
            String width = upperCaseParams.get("WIDTH");
            String height = upperCaseParams.get("HEIGHT");
            wmsRequest = new WMSGetMapRequest(
                    upperCaseParams.get("VERSION"),
                    upperCaseParams.get("FORMAT"),
                    crsParam,
                    upperCaseParams.get("BBOX"),
                    upperCaseParams.get("LAYERS"),
                    upperCaseParams.get("STYLES"),
                    (width == null) ? 500 : Integer.parseInt(width),
                    (height == null) ? 500 : Integer.parseInt(height));
        } else if (requestParam.equals("GetFeatureInfo") == true) {
            String width = upperCaseParams.get("WIDTH");
            String height = upperCaseParams.get("HEIGHT");
            String featureCount = upperCaseParams.get("FEATURECOUNT");
            String i = upperCaseParams.get("I");
            if (i == null) {
                i = upperCaseParams.get("X");
            }
            String j = upperCaseParams.get("J");
            if (j == null) {
                j = upperCaseParams.get("Y");
            }
            wmsRequest = new WMSGetFeatureInfoRequest(
                    upperCaseParams.get("VERSION"),
                    upperCaseParams.get("FORMAT"),
                    crsParam,
                    upperCaseParams.get("BBOX"),
                    upperCaseParams.get("LAYERS"),
                    upperCaseParams.get("STYLE"),
                    (width == null) ? 500 : Integer.parseInt(width),
                    (height == null) ? 500 : Integer.parseInt(height),
                    upperCaseParams.get("QUERY_LAYERS"),
                    upperCaseParams.get("INFO_FORMAT"),
                    (featureCount == null) ? 0 : Integer.parseInt(featureCount),
                    (i == null) ? 0 : Integer.parseInt(i),
                    (j == null) ? 0 : Integer.parseInt(j),
                    upperCaseParams.get("QUERY"));
        } else if ((requestParam.equals("GetKMLData") == true)
                || (requestParam.equals("GetData") == true)) {
            wmsRequest = new WMSGetDataRequest(
                    upperCaseParams.get("VERSION"),
                    upperCaseParams.get("FORMAT"),
                    crsParam,
                    upperCaseParams.get("BBOX"),
                    upperCaseParams.get("LAYERS"),
                    upperCaseParams.get("STYLE"),
                    upperCaseParams.get("QUERY_LAYERS"));
        } else {
            throw new IllegalArgumentException("Not supported request.");
        }

        wmsRequest.setCacheable(upperCaseParams.get("CACHE") == null ? true
                : Boolean.parseBoolean(upperCaseParams.get("CACHE")));
        wmsRequest.setTileX(upperCaseParams.get("TILEX") == null ? 0
                : Integer.parseInt(upperCaseParams.get("TILEX")));
        wmsRequest.setTileY(upperCaseParams.get("TILEY") == null ? 0
                : Integer.parseInt(upperCaseParams.get("TILEY")));
        wmsRequest.setTileZoom(upperCaseParams.get("TILEZOOM") == null ? 0
                : Integer.parseInt(upperCaseParams.get("TILEZOOM")));
        
        
        return wmsRequest;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return this.request;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    public int getTileZoom() {
        return tileZoom;
    }

    public void setTileZoom(int tileZoom) {
        this.tileZoom = tileZoom;
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WMSRequest other = (WMSRequest) obj;
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        if ((this.request == null) ? (other.request != null) : !this.request.equals(other.request)) {
            return false;
        }
        if ((this.format == null) ? (other.format != null) : !this.format.equals(other.format)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.version != null ? this.version.hashCode() : 0);
        hash = 31 * hash + (this.request != null ? this.request.hashCode() : 0);
        hash = 31 * hash + (this.format != null ? this.format.hashCode() : 0);
        return hash;
    }
}
