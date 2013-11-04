/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.Itinerary;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ciberado
 */
public class ItineraryServiceImplHibernate implements ItineraryService {
    
    @PersistenceContext
    private EntityManager em;

    public ItineraryServiceImplHibernate() {
    }

    @Override
    @Transactional
    public Itinerary getItinerary(String code) {
        String jpql = "select i from Itinerary as i "
                    + "join fetch i.features f "
                    + "where i.code = :code ";
        Itinerary itinerary = (Itinerary) em.createQuery(jpql)
                                .setParameter("code", code)
                                .getSingleResult();
        Hibernate.initialize(itinerary);
        return itinerary;
        
    }

    @Override
    @Transactional
    public Set<Itinerary> getItineraryReferences() {
        String jpql = "select i from Itinerary as i order by i.code";
        List<Itinerary> itineraries =  em.createQuery(jpql).getResultList();
        return new LinkedHashSet<Itinerary>(itineraries);
    }
    
    
    
}
