/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.wms.WMSGetMapRequest;
import com.ciberado.botserver.wms.WMSGetMapRequestLayer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * http://localhost:8080/botserver/wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&LAYERS=fitoepisodis&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1550958557555266,41.359636629839656,2.161404296976646,41.365012722558106&WIDTH=512&HEIGHT=512&REASPECT=false
 *
 * @author ciberado
 */
public class ImageMapServImplShape implements ImageMapServ {

    private static final Log log = LogFactory.getLog(ImageMapServImplShape.class);

    private Map<String, FeatureSource> featureSources;

    private Map<String, StyleServ> styleServices;

    public ImageMapServImplShape() {
    }
    
    @PostConstruct
    private void initialize() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
        for (FeatureSource featureSource : featureSources.values()) {
            ReferencedEnvelope area = featureSource.getBounds();
            for (String styleName : styleServices.keySet()) {
                this.drawMap(featureSource, image, styleName, area);
            }
        }
    }

    private BufferedImage drawMap(FeatureSource featureSource, 
                                  BufferedImage image, String styleName,
                                  ReferencedEnvelope areaOfInterest)
    throws IOException {
        return this.drawMap(featureSource, image, styleName, 
                            areaOfInterest.getMinX(), areaOfInterest.getMaxX(), 
                            areaOfInterest.getMinY(), areaOfInterest.getMaxY());
    }
    
    private BufferedImage drawMap(FeatureSource featureSource, 
                                  BufferedImage image, String styleName,
                                  double minx, double maxx, double miny, double maxy)
    throws IOException {
        Graphics2D g = image.createGraphics();
        CoordinateReferenceSystem crs = featureSource.getInfo().getCRS();
        if (crs == null) {
            crs = DefaultGeographicCRS.WGS84;
        }
        //crs = DefaultGeographicCRS.WGS84;
        ReferencedEnvelope activeArea = new ReferencedEnvelope(minx, maxx, miny, maxy, crs);
        MapContext mapContext = new DefaultMapContext();
        mapContext.setTitle(featureSource.getName().getLocalPart());
        Style style = styleServices.get(styleName).getStyle();
        mapContext.addLayer(featureSource, style);

        HashMap hints = new HashMap();

        GTRenderer renderer = new StreamingRenderer();

        RenderingHints hints2d = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        renderer.setJava2DHints(hints2d);

        hints.put("optimizedDataLoadingEnabled", Boolean.TRUE);
        hints.put(RenderingHints.VALUE_RENDER_QUALITY, Boolean.TRUE);
        hints.put(RenderingHints.VALUE_ANTIALIAS_ON, Boolean.TRUE);
        renderer.setRendererHints(hints);
        renderer.setContext(mapContext);

        mapContext.setAreaOfInterest(activeArea);

        Rectangle rectangle = new Rectangle(image.getWidth(), image.getHeight());
        renderer.paint(g, rectangle, activeArea);
        mapContext.dispose();
        return image;
    }


   /** Realiza el proceso de generaci�n de la imagen del mapa requerido. 
     *
     *  Actualmente hay suporte expl�cito para gif y tambi�n se incluye cualquier proveedor
     *  de formato que tenga instalado el sistema (normalmente jpeg y png).
     *
     * @param wmsRequest Petici�n con los datos wms.
     *
     * @throws IOException
     */
    public void processGetMap(WMSGetMapRequest wmsRequest, OutputStream out) throws IOException {
        BufferedImage image = new BufferedImage(wmsRequest.getWidth(),
                    wmsRequest.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (WMSGetMapRequestLayer layer : wmsRequest.getLayers()) {
            FeatureSource source = featureSources.get(layer.getName());

            this.drawMap(source, image, layer.getStyle(),
                    wmsRequest.getBox().getMinx(), wmsRequest.getBox().getMaxx(),
                    wmsRequest.getBox().getMiny(), wmsRequest.getBox().getMaxy());
        }
        Iterator writers = ImageIO.getImageWritersByMIMEType(wmsRequest.getFormat());
        // los jpeg no soportan transparencia -> se la quitamos.
        if (wmsRequest.getFormat().equals("image/jpeg") == true) {
            BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = tmp.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, tmp.getWidth(), tmp.getHeight());
            g.drawImage(image, 0, 0, null);
            image = tmp;
        }
        ImageWriter iw = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        iw.setOutput(ios);
        iw.write(new IIOImage(image, null, null));
        ios.flush();
        iw.dispose();
        out.flush();
        image.flush();

    }

    public void setFeatureSources(Map<String, FeatureSource> featureSources) {
        this.featureSources = featureSources;
    }

    public void setStyles(Map<String, StyleServ> styles) {
        this.styleServices = styles;
    }


}
