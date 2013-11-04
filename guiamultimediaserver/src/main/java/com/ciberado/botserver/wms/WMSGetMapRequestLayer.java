package com.ciberado.botserver.wms;



public class WMSGetMapRequestLayer {
	private String name;
	private String style;
	
	public WMSGetMapRequestLayer() {
		this(null,null);
	}
	
	public WMSGetMapRequestLayer(String name, String style) {
		super();
		this.setName(name);
		this.setStyle(style);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getStyle() {
		return style;
	}
	
	
}
