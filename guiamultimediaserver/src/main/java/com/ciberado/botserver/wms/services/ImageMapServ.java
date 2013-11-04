/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.wms.WMSGetMapRequest;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author ciberado
 */
public interface ImageMapServ {

        void processGetMap(WMSGetMapRequest wmsRequest, OutputStream out) throws IOException;


}
