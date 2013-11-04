package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.wms.WMSGetFeatureInfoRequest;
import java.awt.geom.NoninvertibleTransformException;
import java.io.IOException;
import java.io.PrintWriter;

/** Permite retornar información sobre un determinado punto (píxel) del mapa.
 *
 *
 */
public interface FeatureInfoServ {
    
    public void processGetFeatureInfo(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out)
    throws IOException, NoninvertibleTransformException;


    public void processFeatureSearch(WMSGetFeatureInfoRequest wmsRequest, PrintWriter out);

    
}
