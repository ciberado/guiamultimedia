/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing.dto;

import com.ciberado.botserver.model.Itinerary;
import com.ciberado.botserver.model.ItineraryFeature;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 *
 * @author ciberado
 */
public class ItineraryDTO {

    private String code;
    private String name;
    private String description;
    private String audioFile;
    
    private Set<ItineraryFeatureDTO> features;

    
    public ItineraryDTO(Itinerary itinerary, Locale locale) {
        this(itinerary, locale, false);
    }
    
    public ItineraryDTO(Itinerary itinerary, Locale locale, boolean includeFeatures) {
        this.code = itinerary.getCode();
        this.name = itinerary.getNameResources().get(locale.getLanguage());
        this.description = itinerary.getDescriptionResources().get(locale.getLanguage());
        this.audioFile = itinerary.getAudioFileResources().get(locale.getLanguage());        
        if (includeFeatures == true) {
            features = new LinkedHashSet<ItineraryFeatureDTO>();
            for (ItineraryFeature feature : itinerary.getFeatures()) {
                ItineraryFeatureDTO featureDTO = new ItineraryFeatureDTO(feature, locale);
                features.add(featureDTO);
            }
        }
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Set<ItineraryFeatureDTO> getFeatures() {
        return features;
    }
    
    
}
