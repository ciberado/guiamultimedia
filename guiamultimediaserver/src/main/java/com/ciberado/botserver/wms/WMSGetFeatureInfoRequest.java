package com.ciberado.botserver.wms;



/** Implementa la petición WMS para un GetFeatureInfo. 
 * 
 */
public class WMSGetFeatureInfoRequest extends WMSRequest {
	/** Formato de respuesta de las features. Se soporta text/xml y text/html. Cualquier
	 *  otro formato se retorna como text/xml.
	 */
	private String infoFormat;
	/** N�mero de features requerido. Actualmente se ignora. */
	private int featureCount;
	/** Coordenada horizontal sobre el CRS del mapa 
         *  o bien sobre el ancho de la imagen si no se utiliza query. */
	private int i;
	/** Coordenada vertical sobre el CRS del mapa 
         *  o bien sobre el alto de la imagen si nos e utiliza query. */
	private int j;
	/** Lista de layers sobre los que se requiere informaci�n. */
	WMSGetMapRequestLayer[] queryLayers;
        /** Extensi�n de wms propia para filtros. */
        private String query;
	/** Las peticiones GetFeatureInfo incluyen todos los datos de una GetMap. En este
	 *  atributo se almacenan esa parte.
	 */
	private WMSGetMapRequest mapRequest;
	
	public WMSGetFeatureInfoRequest() {
		this(null, null, null, null, null, null, 0, 0, null, null, 0, 0, 0, null);
	}
	
	public WMSGetFeatureInfoRequest(String version, String format, String crs,
	         String boxDef, String layersDef, String stylesDef,
	         int width, int height, String queryLayersDef, String infoFormat,
	         int featureCount, int i, int j, String query) {
		super(version, "GetFeatureInfo", format);
		setMapRequest(new WMSGetMapRequest(
				version, format, crs, boxDef, layersDef, stylesDef, width, height));
		this.setFeatureCount(featureCount); 
		this.parseQueryLayers(queryLayersDef);
		this.setInfoFormat(infoFormat);
		this.setI(i);
		this.setJ(j);
                this.setQuery(query);
	}

	private void parseQueryLayers(String queryLayersDef) {
		String[] layerNames = queryLayersDef.split(",");
		int freeSlot = 0;
		this.queryLayers = new WMSGetMapRequestLayer[layerNames.length];
		for (int idx=0; idx < mapRequest.getLayers().length; idx++) {
			WMSGetMapRequestLayer layer = mapRequest.getLayers()[idx];
			boolean found = false;
			for (int jdx=0; (jdx <layerNames.length) && (found == false); jdx++) {
				found = layer.getName().equals(layerNames[jdx]);
			}
			if (found == true) {	
				this.queryLayers[freeSlot] = layer;
				freeSlot = freeSlot + 1;
			}
		}
	}
	
	public WMSGetMapRequestLayer[] getQueryLayers() {
		return this.queryLayers;
	}

	private void setI(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

	private void setJ(int j) {
		this.j = j;
	}

	public int getJ() {
		return j;
	}

	public void setMapRequest(WMSGetMapRequest mapRequest) {
		this.mapRequest = mapRequest;
	}

	public WMSGetMapRequest getMapRequest() {
		return mapRequest;
	}

	public void setFeatureCount(int featureCount) {
		this.featureCount = featureCount;
	}

	public int getFeatureCount() {
		return featureCount;
	}

	public void setInfoFormat(String infoFormat) {
		this.infoFormat = infoFormat;
	}

	public String getInfoFormat() {
		return infoFormat;
	}

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }


	
}
