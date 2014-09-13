/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author ciberado
 */
@Entity
@org.hibernate.annotations.Entity(mutable=false)
@Table(name="taxa")
@SecondaryTables({
    @SecondaryTable(name="t_taxa_media"),
    @SecondaryTable(name="t_taxa_bio")    
})
public class Taxon implements Serializable {
    
    private static final long serialVersionUID = 1L;

        
    @Id
    @Column(name="codi_esp", length=16)
    private String code;    
    
    @Column(name="familia", length=64)
    private String family;
    
    @Column(name="genere", length=64)
    private String genus;    
    
    @Column(name="especie", length=64)
    private String speciesName;    

    @Column(name="autor_sp", length=64)
    private String speciesAuthor;    

    @Column(name="subespecie", length=64)
    private String subspecies;    

    @Column(name="autor_ssp", length=64)
    private String subspeciesAuthor;    

    @Column(name="zona_mon", length=150)
    private String worldLocation;    

    @Column(name="nom", length=100)
    private String popularNameCA;    

    @Column(name="nombre", length=100)
    private String popularNameES;    

    @Column(name="name", length=100)
    private String popularNameEN;
    
    @ElementCollection(fetch= FetchType.EAGER)
    @CollectionTable(name = "t_taxa_desc", 
                     joinColumns = {@JoinColumn(name = "codi_esp")})
    @MapKeyColumn(name = "ta_desc_idioma", columnDefinition = "bpchar", length=2)
    private Map<String, DescriptionResource> resources = 
            new HashMap<String, DescriptionResource>();
   
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_taxa_media",             
                     joinColumns = @JoinColumn(name = "codi_esp"))
    @Column(name = "ta_me_fitxer")
    private Set<String> media = new LinkedHashSet<String>();
    
    @Column(table="t_taxa_bio", name = "port_aplic")
    private String port;
    
    public Taxon() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getPopularNameCA() {
        return popularNameCA;
    }

    public void setPopularNameCA(String popularNameCA) {
        this.popularNameCA = popularNameCA;
    }

    public String getPopularNameEN() {
        return popularNameEN;
    }

    public void setPopularNameEN(String popularNameEN) {
        this.popularNameEN = popularNameEN;
    }

    public String getPopularNameES() {
        return popularNameES;
    }

    public void setPopularNameES(String popularNameES) {
        this.popularNameES = popularNameES;
    }

    public String getSpeciesAuthor() {
        return speciesAuthor;
    }

    public void setSpeciesAuthor(String speciesAuthor) {
        this.speciesAuthor = speciesAuthor;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public String getSubspecies() {
        return subspecies;
    }

    public void setSubspecies(String subspecies) {
        this.subspecies = subspecies;
    }

    public String getSubspeciesAuthor() {
        return subspeciesAuthor;
    }

    public void setSubspeciesAuthor(String subspeciesAuthor) {
        this.subspeciesAuthor = subspeciesAuthor;
    }

    public String getWorldLocation() {
        return worldLocation;
    }

    public void setWorldLocation(String worldLocation) {
        this.worldLocation = worldLocation;
    }

    @Transient private Map<String, String> descResources = new HashMap<String, String>();    
    public Map<String, String> getDescriptionResources() {            
        if (resources == null) return null;
        
        if (descResources.isEmpty()) for (String lang : resources.keySet()) {
            descResources.put(lang, resources.get(lang).description);
        }
        return descResources;
    }
    
    @Transient private Map<String, String> wikipediaResources = new HashMap<String, String>();    
    public Map<String, String> getWikipediaResource() {            
        if (resources == null) return null;
        
        if (wikipediaResources.isEmpty()) for (String lang : resources.keySet()) {
            wikipediaResources.put(lang, resources.get(lang).wikipedia);
        }
        return wikipediaResources;
    }
    
    @Transient private Map<String, String> audioFileResources = new HashMap<String, String>();    
    public Map<String, String> getAudioFileResources() {            
        if (resources == null) return null;
        
        if (audioFileResources.isEmpty()) for (String lang : resources.keySet()) {
            audioFileResources.put(lang, resources.get(lang).audio);
        }
        return audioFileResources;
    }

    
    public Set<String> getMedia() {
        return media;
    }

    public void setMedia(Set<String> media) {
        this.media = media;
    }

    @JsonIgnore
    public Map<String, DescriptionResource> getResources() {
        return resources;
    }

    public void setResources(Map<String, DescriptionResource> resources) {
        this.resources = resources;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Taxon other = (Taxon) obj;
        if ((this.code == null) ? (other.code != null) : !this.code.equals(other.code)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (this.code != null ? this.code.hashCode() : 0);
        return hash;
    }

    @Embeddable    
    public static class DescriptionResource implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @Column(name = "ta_desc_text")
        private String description;
        @Column(name = "ta_desc_audio")
        private String audio;
        @Column(name = "ta_desc_etimologia")
        private String etimologia;
        @Column(name = "ta_desc_wikipedia")
        private String wikipedia;
        
    }

    @Override
    public String toString() {
        return "Taxon{" + "code=" + code + ", family=" + family + ", genus=" + genus + ", speciesName=" + speciesName + ", speciesAuthor=" + speciesAuthor + ", subspecies=" + subspecies + ", subspeciesAuthor=" + subspeciesAuthor + ", worldLocation=" + worldLocation + ", popularNameCA=" + popularNameCA + ", popularNameES=" + popularNameES + ", popularNameEN=" + popularNameEN + ", resources=" + resources + ", media=" + media + ", descResources=" + descResources + ", audioFileResources=" + audioFileResources + '}';
    }
    
    
    
}
