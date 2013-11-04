/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing.dto;

import com.ciberado.botserver.model.ItineraryFeature;
import java.util.Locale;

/**
 *
 * @author ciberado
 */
public class ItineraryFeatureDTO {

    private String name;
    private String description;
    private String audioFile;
    private double lat;
    private double lon;

    public ItineraryFeatureDTO(ItineraryFeature feature, Locale locale) {
        this.name = feature.getNameResources().get(locale.getLanguage());
        this.description = feature.getDescriptionResources().get(locale.getLanguage());
        this.audioFile = feature.getAudioFileResources().get(locale.getLanguage());        
        this.lat = feature.getLat();
        this.lon = feature.getLon();
    }


    public String getAudioFile() {
        return audioFile;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    
    
}
