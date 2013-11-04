/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.Specimen;
import java.awt.geom.Point2D;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author ciberado
 */
public class FeatureInfoServiceImplHibernate 
implements FeatureInfoService  {
    
    private double MAX_FEATURE_DISTANCE_TO_CENTER = 5.0; //meters
    protected static final int MAX_FEATURES = 10;
    
    @PersistenceContext
    private EntityManager em;

    public FeatureInfoServiceImplHibernate() {
    }

    // http://localhost:8080/guiamultimediaserver/rest/features/near?callback=callback&lat=41.36224329471588&lon=2.1598930656909943&maxDistance=2&_=1383004202836
    @Transactional
    @Override
    public List<Specimen> getSpecimens(double centerLat, double centerLon, double maxDistance) {
        if (maxDistance <= 0) {
            maxDistance = MAX_FEATURE_DISTANCE_TO_CENTER;
        }
        GeodeticCalculator gc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
        gc.setStartingGeographicPoint(centerLon, centerLat);
        gc.setDirection(-45, maxDistance);
        Point2D topLeft = gc.getDestinationGeographicPoint();
        gc.setDirection(+45+90, maxDistance);
        Point2D bottomRight = gc.getDestinationGeographicPoint();
        // join media etc todo TODO TODO TODO TODO 
        String jpql = "select s from Specimen as s left join fetch s.taxon "
                    + "where s.lat between :minLat and :maxLat " 
                    + "  and s.lon between :minLon and :maxLon "
                    + "order by (s.lat - :centerLat)*(s.lat - :centerLat) + "
                    + "         (s.lon - :centerLon)*(s.lon - :centerLon)";
        List<Specimen> specimens = em.createQuery(jpql)
                                     .setParameter("minLon", topLeft.getX())
                                     .setParameter("maxLon", bottomRight.getX())
                                     .setParameter("minLat", bottomRight.getY())
                                     .setParameter("maxLat", topLeft.getY())
                                     .setParameter("centerLat", centerLat)
                                     .setParameter("centerLon", centerLon)
                                     .setMaxResults(MAX_FEATURES)
                                     .getResultList();            
        return specimens; 
    }

    // http://localhost:8080/guiamultimediaserver/rest/features/bbox?callback=callback&minLat=41.36168530752659&minLon=2.157649980688095&maxLat=41.36198530752659&maxLon=2.157949980688095&_=1383001961356
    @Transactional
    @Override
    public List<Object> getSpecimenReferences(double minLat, double minLon, 
                                                double maxLat, double maxLon) {        
        //String jpql = "select new map(s.lat as lat, s.lon as lon, t.port as port) "
        String sql =  "select distinct s.lat, s.lon, t.port_aplic  "
                    + "from t_inventari as s left join t_taxa_bio as t "
                    + "on s.codi_esp = t.codi_esp "
                    + "where s.active = true "
                    + "  and s.lat between :minLat and :maxLat " 
                    + "  and s.lon between :minLon and :maxLon ";
        List<Object> specimens = em.createNativeQuery(sql)
                                     .setParameter("minLon", minLon)
                                     .setParameter("maxLon", maxLon)
                                     .setParameter("minLat", minLat)
                                     .setParameter("maxLat", maxLat)
                                     //.setMaxResults(MAX_FEATURES)
                                     .getResultList();            
        return specimens; 
    }

    @Transactional
    @Override
    public List<Specimen> getSpecimens(String pattern, 
                                       double centerLat, double centerLon) {
        pattern = "%" + pattern.toLowerCase().replaceAll(" ", "%") + "%";

        String jpql =  "select s from Specimen s join fetch s.taxon  "
                        + "where s.keywords like :value "
                        + "order by (s.lat - :centerLat)*(s.lat - :centerLat) + "
                        + "         (s.lon - :centerLon)*(s.lon - :centerLon)";
        List<Specimen> specimens = em.createQuery(jpql)
                                        .setParameter("value", pattern)
                                        .setParameter("centerLat", centerLat)
                                        .setParameter("centerLon", centerLon)
                                        .setMaxResults(MAX_FEATURES)
                                        .getResultList();
        return specimens;
    }

    
}
