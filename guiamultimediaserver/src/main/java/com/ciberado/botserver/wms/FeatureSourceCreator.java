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
public class FeatureSourceCreator  {

    private String shapePath;
    private FileDataStore store;

    public FeatureSourceCreator(String shapePath) {
        this.shapePath = shapePath;
    }

    public FeatureSource createFeatureSource() throws IOException {
        File file = new File(shapePath);
        if (this.store == null) {
            this.store = FileDataStoreFinder.getDataStore(file);
        }
        FeatureSource featureSource = store.getFeatureSource();

        //featureSource = new CachingFeatureSource(featureSource);
        return featureSource;
    }
}
