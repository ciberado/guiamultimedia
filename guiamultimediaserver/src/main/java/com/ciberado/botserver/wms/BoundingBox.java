package com.ciberado.botserver.wms;

/** Conjunto de coordenadas utm que determinan una región rectangular (box). */
public class BoundingBox {
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;

	public BoundingBox(String boxDef) {
		super();
		
		String[] chunks = boxDef.split(",") ;
		this.setMinx(Double.parseDouble(chunks[0].trim()));
		this.setMiny(Double.parseDouble(chunks[1].trim()));
		this.setMaxx(Double.parseDouble(chunks[2].trim()));
		this.setMaxy(Double.parseDouble(chunks[3].trim()));
	}
	
	public BoundingBox(double minx, double miny, double maxx, double maxy) {
		super();
                
                if ((minx > maxx) ||(miny > maxy)) {
                    throw new IllegalArgumentException("Nonsense parameters.");
                }
                
		this.setMinx(minx);
		this.setMiny(miny);
		this.setMaxx(maxx);
		this.setMaxy(maxy); 
	}

	public boolean containsPoint(long x, long y) {
		return ((x >= minx) && (x <= maxx) && (y >= miny) && (y <= maxy));
	}
	
	public void setMinx(double minx) {
		this.minx = minx;
	}

	public double getMinx() {
		return minx;
	}

	public void setMiny(double miny) {
		this.miny = miny;
	}

	public double getMiny() {
		return miny;
	}

	public void setMaxx(double maxx) {
		this.maxx = maxx;
	}

	public double getMaxx() {
		return maxx;
	}

	public void setMaxy(double maxy) {
		this.maxy = maxy;
	}

	public double getMaxy() {
		return maxy;
	}
	
}

