/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms;

import java.io.File;
import java.io.IOException;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;

/**
 *
 * @author ciberado
 */
public class FeatureSourceFactory  {


    public static FeatureSource create(String shapePath) throws IOException {
        File file = new File(shapePath);
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();
        return featureSource;
    }
}
