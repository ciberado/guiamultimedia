/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import java.io.IOException;
import org.jdom.Document;

/**
 *
 * @author ciberado
 */
public interface CapabilitiesServ {

       Document getCapabilities() throws IOException;

}
