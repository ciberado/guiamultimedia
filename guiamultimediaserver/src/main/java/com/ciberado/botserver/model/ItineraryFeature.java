package com.ciberado.botserver.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;

/**
 *
 * @author ciberado
 */
@Entity
@Table(name="t_itineraris_elems")
@SecondaryTable(name="t_itineraris_elems_desc", pkJoinColumns= {
                        @PrimaryKeyJoinColumn(name="it_codi", referencedColumnName="it_codi"),
                        @PrimaryKeyJoinColumn(name="it_elem_ordre", referencedColumnName="it_elem_ordre")
                     })
public class ItineraryFeature implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private ItineraryFeatureIdentity identity;
    
    @ElementCollection(fetch= FetchType.EAGER)
    @CollectionTable(name = "t_itineraris_elems_desc", 
                     joinColumns = {@JoinColumn(name = "it_codi"), @JoinColumn(name = "it_elem_ordre")})
    @MapKeyColumn(name = "it_elem_desc_idioma", columnDefinition = "bpchar", length=2)
    @Column(name = "it_elem_desc_nom")
    private Map<String, ItineraryFeatureResources> resources = new HashMap<String, ItineraryFeatureResources>();
    
    @Column(name="it_elem_lat")
    private double lat;
    
    @Column(name="it_elem_lon")
    private double lon;
    
    public ItineraryFeature() {
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItineraryFeature other = (ItineraryFeature) obj;
        if (this.identity.itinerary != other.identity.itinerary && (this.identity.itinerary == null || !this.identity.itinerary.equals(other.identity.itinerary))) {
            return false;
        }
        if (this.identity.order != other.identity.order) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.identity.itinerary != null ? this.identity.itinerary.hashCode() : 0);
        hash = 97 * hash + this.identity.order;
        return hash;
    }

    @Embeddable
    private static class ItineraryFeatureIdentity implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ManyToOne
        @JoinColumn(name="it_codi")
        private Itinerary itinerary;

        @Column(name="it_elem_ordre")
        private int order;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ItineraryFeatureIdentity other = (ItineraryFeatureIdentity) obj;
            if (this.itinerary != other.itinerary && (this.itinerary == null || !this.itinerary.equals(other.itinerary))) {
                return false;
            }
            if (this.order != other.order) {
                return false;
            }
            return true;
        }
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.itinerary != null ? this.itinerary.hashCode() : 0);
            hash = 79 * hash + this.order;
            return hash;
        }        
        
    }
 
    @Embeddable
    public static class ItineraryFeatureResources implements Serializable {
        @Column(name = "it_elem_desc_nom")
        private String name;
        @Column(name = "it_elem_desc_text")
        private String description;
        @Column(name = "it_elem_desc_audio")
        private String audio;

        public ItineraryFeatureResources() {
        }
    }

}
