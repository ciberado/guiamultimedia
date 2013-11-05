/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.Specimen;
import java.util.List;

/**
 *
 * @author ciberado
 */
public interface FeatureInfoService {

    List<Specimen> getSpecimens(double lat, double lon, double maxDistance);
    
    List<Object> getSpecimenReferences(double minLat, double minLon, double maxLat, double maxLon);
    
    List<Specimen> getSpecimens(String pattern, double lat, double lon);
    
}
