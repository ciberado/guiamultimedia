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
public class InsertLatLon {
    
    public static void main(String[] args) throws Exception {

        String shapePath = "C:/datos/My Dropbox/ws/botguia/botserver-jardibotanic/src/META-INF/maps/itiaud_punts_lonlatwgs84.shp";
        File file = new File(shapePath);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();
        System.out.println("Feature Source ready.");
        
        //String url = "jdbc:postgresql://localhost/botanicdb";
        String url = "jdbc:postgresql://172.31.5.68/botanicdb";
        Connection con = DriverManager.getConnection(url, "postgres", "141414");
        con.setAutoCommit(false);
        System.out.println("Connection ready.");
        
        String sql = "UPDATE t_itineraris_elems SET it_elem_lat = ?, it_elem_lon = ? "
                   + "WHERE it_elem_ordre = ?";
        PreparedStatement stmt = con.prepareCall(sql);
        FeatureIterator iter = featureSource.getFeatures().features();
        while (iter.hasNext() == true) {
            SimpleFeature feature = (SimpleFeature) iter.next();
            GeometryAttribute geoAttr = feature.getDefaultGeometryProperty();
            Point point = (Point) geoAttr.getValue();
            double lat = point.getCoordinate().y;
            double lon = point.getCoordinate().x;
            String codiInd = String.valueOf(feature.getAttribute("obs"));
            stmt.setDouble(1, lat);
            stmt.setDouble(2, lon);
            stmt.setInt(3, Integer.parseInt(codiInd));
            int mod = stmt.executeUpdate();
            System.out.print(mod + ", ");
        }
        System.out.println();
        con.commit();
        stmt.close();
        con.close();
        
        System.out.println("Fin.");
    }
    
}
