/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

@Entity
@Table(name="t_itineraris")
public class Itinerary implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="it_codi")
    private String code;

    @ElementCollection(fetch= FetchType.EAGER)
    @CollectionTable(name = "t_itineraris_desc", 
                     joinColumns = {@JoinColumn(name = "it_codi")})
    @MapKeyColumn(name = "it_desc_idioma", columnDefinition = "bpchar", length=2)
    @Column(name = "it_desc_nom")
    private Map<String, ItineraryResources> resources = new HashMap<String, ItineraryResources>();

    
    @OneToMany(mappedBy="identity.itinerary")
    @OrderColumn(name="it_elem_ordre")
    private List<ItineraryFeature> features = 
               new ArrayList<ItineraryFeature>();

    public Itinerary() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Transient private Map<String, String> nameResources = new HashMap<String, String>();    
    public Map<String, String> getNameResources() {            
        if (nameResources.isEmpty()) for (String lang : resources.keySet()) {
            nameResources.put(lang, resources.get(lang).name);
        }
        return nameResources;
    }
    
    @Transient private Map<String, String> descResources = new HashMap<String, String>();    
    public Map<String, String> getDescriptionResources() {            
        if (descResources.isEmpty()) for (String lang : resources.keySet()) {
            descResources.put(lang, resources.get(lang).description);
        }
        return descResources;
    }

    @Transient private Map<String, String> audioFileResources = new HashMap<String, String>();    
    public Map<String, String> getAudioFileResources() {            
        if (audioFileResources.isEmpty()) for (String lang : resources.keySet()) {
            audioFileResources.put(lang, resources.get(lang).audio);
        }
        return audioFileResources;
    }
    

    public List<ItineraryFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<ItineraryFeature> features) {
        this.features = features;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Itinerary other = (Itinerary) obj;
        if ((this.code == null) ? (other.code != null) : !this.code.equals(other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }


    @Embeddable
    public static class ItineraryResources implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "it_desc_nom")
        private String name;
        @Column(name = "it_desc_text")
        private String description;
        @Column(name = "it_desc_audio")
        private String audio;
    }



}
