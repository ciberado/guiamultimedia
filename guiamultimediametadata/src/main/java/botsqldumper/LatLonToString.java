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
public class LatLonToString {
    
    public static void main(String[] args) throws Exception {

        String shapePath = "C:/datos/My Dropbox/ws/botguia/botserver-jardibotanic/src/META-INF/maps/itiaud_path_lonlatwgs84.shp";
        File file = new File(shapePath);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();
        System.out.println("Feature Source ready.");
        
        FeatureIterator iter = featureSource.getFeatures().features();
        while (iter.hasNext() == true) {
            SimpleFeature feature = (SimpleFeature) iter.next();
            GeometryAttribute geoAttr = feature.getDefaultGeometryProperty();
            Point point = (Point) geoAttr.getValue();
            double lat = point.getCoordinate().y;
            double lon = point.getCoordinate().x;
            
            System.out.print(lat + " " + lon + ", ");
        }
        System.out.println();
        System.out.println("Fin.");
    }
    
}
