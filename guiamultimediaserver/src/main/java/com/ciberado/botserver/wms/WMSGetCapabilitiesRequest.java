package com.ciberado.botserver.wms;



/** Implementa la peticion WMS para un GetCapabilities. 
 * 
 */
public class WMSGetCapabilitiesRequest extends WMSRequest {

	public WMSGetCapabilitiesRequest() {
		this(null,null);
	}

	public WMSGetCapabilitiesRequest(String version, String format) {
		super(version, "GetCapabilities", format);
	}

	public String getRequest() {
		return "GetCapabilities";
	}
	
	

}
