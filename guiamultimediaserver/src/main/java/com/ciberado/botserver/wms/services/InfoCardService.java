/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.datapublishing.InfoCard;

/**
 *
 * @author ciberado
 */
public interface InfoCardService {
    
    InfoCard getInfoCard(String infoCardId);
    
}
