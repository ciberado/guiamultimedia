package com.ciberado.botserver.wms;

/** Implementa la peticin WMS para un GetCapabilities. 
 * 
 */
public class WMSGetDataRequest extends WMSRequest {

    private WMSGetMapRequest mapRequest;

    public WMSGetDataRequest() {
            this(null, null, null, null, null, null, null);
    }

    public WMSGetDataRequest(String version, String format, String crs,
             String boxDef, String layersDef, String stylesDef, String queryLayersDef) {
            super(version, "GetData", format);
            this.setMapRequest(new WMSGetMapRequest(version, format, crs, boxDef, layersDef, stylesDef, 0, 0));
    }

    public WMSGetMapRequest getMapRequest() {
        return mapRequest;
    }

    public void setMapRequest(WMSGetMapRequest mapRequest) {
        this.mapRequest = mapRequest;
    }
	
	
	

}
