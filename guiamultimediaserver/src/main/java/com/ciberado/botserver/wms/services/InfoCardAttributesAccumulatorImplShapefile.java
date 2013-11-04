/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.datapublishing.InfoCard;
import com.ciberado.botserver.util.ShpDumper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;

/**
 *
 * @author ciberado
 */
public class InfoCardAttributesAccumulatorImplShapefile
implements InfoCardAttributesAccumulator {

    private static final Log log = LogFactory.getLog(InfoCardAttributesAccumulator.class);
    
    private FeatureSource featureSource;
    private String keyFieldName;
    private Map<String /* featureIdField */, Map<String /* FieldName */, Object /* Value */>> data;


    public InfoCardAttributesAccumulatorImplShapefile(FeatureSource featureSource, String keyFieldName) 
    throws IOException {
        this.featureSource = featureSource;
        this.keyFieldName = keyFieldName;
        log.info("Loading shape into memory cache in concurrent thread.");
        new Thread(new RunnableImpl(keyFieldName)).start();
    }

    @Override
    public void addAttributes(String key, InfoCard infoCard) {
        while (this.data == null) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        Map<String, Object> rowData = data.get(key);
        if (rowData != null) for (Map.Entry<String, Object> entries : rowData.entrySet()) {
            infoCard.set(entries.getKey(), entries.getValue());
        }
    }

    public String getKeyFieldName() {
        return this.keyFieldName;
    }
    
    

    class RunnableImpl implements Runnable {

        private final String keyFieldName;

        public RunnableImpl(String keyFieldName) {
            this.keyFieldName = keyFieldName;
        }

        public void run() {
            synchronized (InfoCardAttributesAccumulatorImplShapefile.this) {
                try {
                    ShpDumper dumper = new ShpDumper();
                    Map<String, Map<String, Object>> data = 
                        new HashMap<String, Map<String, Object>>();
                    dumper.bulkShpDumper(featureSource, keyFieldName, data);
                    InfoCardAttributesAccumulatorImplShapefile.this.data = data;
                    InfoCardAttributesAccumulatorImplShapefile.this.notifyAll();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    

}

