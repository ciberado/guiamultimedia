/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package botsqldumper;

import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author ciberado
 */
public class InsertLatLonPunts {
    
    public static void main(String[] args) throws Exception {

        String shapePath = "C:/Users/ciberado/Downloads/shp-gpm/shp-gpm/XYT_GPS-WGS84-miramon.shp";
        File file = new File(shapePath);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();
        System.out.println("Feature Source ready.");
        
        //String url = "jdbc:postgresql://localhost/botanicdb";
        String url = "jdbc:postgresql://172.31.5.68/botanicdb";
        Connection con = DriverManager.getConnection(url, "postgres", "141414");
        con.setAutoCommit(false);
        System.out.println("Connection ready.");
        
        String sql = "INSERT INTO t_inventari (id, codi_ind, codi_esp, lat, lon) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = con.prepareStatement(sql);
        FeatureIterator iter = featureSource.getFeatures().features();
        int count = 0;
        while (iter.hasNext() == true) {
            SimpleFeature feature = (SimpleFeature) iter.next();
            GeometryAttribute geoAttr = feature.getDefaultGeometryProperty();
            Point point = (Point) geoAttr.getValue();
            double lat = point.getCoordinate().y;
            double lon = point.getCoordinate().x;
            String codiInd = String.valueOf(feature.getAttribute("codi_ind")).trim();
            String codiEsp = String.valueOf(feature.getAttribute("codi_esp")).trim();
            stmt.setInt(1, Integer.parseInt(codiInd));
            stmt.setInt(2, Integer.parseInt(codiInd));
            stmt.setString(3, codiEsp);
            stmt.setDouble(4, lat);
            stmt.setDouble(5, lon);
            stmt.executeUpdate();
            count = count + 1;
        }
        System.out.println();
        System.out.println("Insertados: " + count);
        con.commit();
        stmt.close();
        con.close();
        
        System.out.println("Fin.");
    }
    
}
