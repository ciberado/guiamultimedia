/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botmetadata;

import java.io.File;

/**
 *
 * @author ciberado
 */
public interface DataUpdater {

    
    void begin();
    
    void updateInfo(File originalFile, String speciesName, String authorName, String regionType) ;
    
    void end(boolean ok);
    
}
