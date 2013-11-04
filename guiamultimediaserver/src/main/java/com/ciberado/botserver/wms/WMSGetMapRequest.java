package com.ciberado.botserver.wms;



/** Implementa la petici�n WMS para un GetMap. 
 * 
 */
public class WMSGetMapRequest extends WMSRequest {
	
	/** Nombre del sistema de referencia utilizado (normalmente EPSG:23031). */ 
	private String  crs;
	/** Regi�n del crs que desea el cliente. */
	private BoundingBox box;
	/** Layers requeridos (en nuestro caso corresponden al CODI_E_ORC de los taxons. */
	private WMSGetMapRequestLayer[] layers;
	/** Ancho del mapa en p�xels. */
	private int width;
	/** Alto del mapa en p�xels. */
	private int height;
	
	public WMSGetMapRequest() {
		this(null, null, null, null,  null, null, 0, 0);
	}
	
	public WMSGetMapRequest(String version, String format, String crs,
			         String boxDef, String layersDef, String stylesDef,
			         int width, int height) {		
		super(version, "GetMap", format);
		this.setCRS(crs);
		if (boxDef != null) {
			this.setBox(new BoundingBox(boxDef));
		} 
		if (layersDef != null) {
			this.parseLayers(layersDef, stylesDef);
		}
		
		this.setWidth(width);
		this.setHeight(height);
		
	}

	public void setBox(BoundingBox box) {
		this.box = box;
	}

	public BoundingBox getBox() {
		return box;
	}

	private void parseLayers(String layersDef, String stylesDef) {
		String[] layerNames = layersDef.split(",");
		String[] stylesNames = ((stylesDef == null) || (stylesDef.length()==0)) ? 
				               null : stylesDef.replaceAll(",", ", ").split(",");
		this.layers = new WMSGetMapRequestLayer[layerNames.length];
		for (int idx=0; idx < layerNames.length; idx++) {
			WMSGetMapRequestLayer layer = new WMSGetMapRequestLayer();
			layer.setName(layerNames[idx]);
			if ((stylesNames == null) || 
			   (stylesNames.length < idx) || 
			   (stylesNames[idx].trim().length() == 0)) {
				layer.setStyle("default");
			} else {
				layer.setStyle(stylesNames[idx].trim());
			}
			this.layers[idx] = layer;
		}
	}

	public WMSGetMapRequestLayer[] getLayers() {
		return layers;
	}
        
        public String getLayerNames() {
            StringBuilder names = new StringBuilder();
            for (WMSGetMapRequestLayer current : getLayers()) {
                names.append(current.getName() + ", ");
            }
            if (names.length() > 0) {
                names.setLength(names.length()-2);
            }
            
            return names.toString();
        }

	public void setCRS(String crs) {
		this.crs = crs;
	}

	public String getCRS() {
		return crs;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

        

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WMSGetMapRequest other = (WMSGetMapRequest) obj;
        if ((this.crs == null) ? (other.crs != null) : !this.crs.equals(other.crs)) {
            return false;
        }
        if (this.box != other.box && (this.box == null || !this.box.equals(other.box))) {
            return false;
        }
        if (this.width != other.width) {
            return false;
        }
        if (this.height != other.height) {
            return false;
        }
        for (int idx = 0; idx < layers.length; idx++) {
            WMSGetMapRequestLayer layer = layers[idx];
            if (layer.equals(other.layers[idx]) == false) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (this.crs != null ? this.crs.hashCode() : 0);
        hash = 11 * hash + (this.box != null ? this.box.hashCode() : 0);
        hash = 11 * hash + this.width;
        hash = 11 * hash + this.height;
        for (WMSGetMapRequestLayer layer : layers) {
            hash = 11 * hash + layer.hashCode();
        }
        return hash;
    }

        
        
	
}
