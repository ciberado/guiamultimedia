/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author ciberado
 */
@Entity
@org.hibernate.annotations.Entity(mutable=false)
@Table(name="t_inventari")
@org.hibernate.annotations.Where(
        clause="active = 'true'")
public class Specimen implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name="codi_ind" )
    private int code;
    
    private double lat;
    
    private double lon;
    
    @ManyToOne(fetch= FetchType.LAZY, optional=true)
    @JoinColumn(name="codi_esp")
    private Taxon taxon;
    
    @Basic(fetch= FetchType.LAZY)
    private String keywords;

    public Specimen() {
    }
    
    public Specimen(int code, double lat, double lon, String port) {
        this.code = code;
        this.lat = lat;
        this.lon = lon;
        this.taxon = new Taxon();
        this.taxon.setPort(port);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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

    public Taxon getTaxon() {
        return taxon;
    }

    public void setTaxon(Taxon taxon) {
        this.taxon = taxon;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Specimen other = (Specimen) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.code;
        return hash;
    }
    
    
    
}
