/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.Itinerary;
import java.util.Set;

/**
 *
 * @author ciberado
 */
public interface ItineraryService {

    public Itinerary getItinerary(String code);

    public Set<Itinerary> getItineraryReferences();    

}
