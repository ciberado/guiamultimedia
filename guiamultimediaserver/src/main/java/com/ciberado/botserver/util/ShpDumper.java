/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.util;

import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 *
 * @author ciberado
 */
public class ShpDumper {

    private static final Log log = LogFactory.getLog(ShpDumper.class);

    public void bulkShpDumper(FeatureSource featureSource, String featureIdField,
            Map<String /* featureIdField */, Map<String /* FieldID */, Object /* Value */>> data)
            throws IOException {
        long t0 = System.currentTimeMillis();
        log.info("Loading shapefile into a memory cache.");
        FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = featureSource.getFeatures();
        FeatureIterator<SimpleFeature> iterator = selectedFeatures.features();
        List<AttributeDescriptor> descriptors = null;
        while (iterator.hasNext() == true) {
            SimpleFeature feature = iterator.next();
            if (descriptors == null) {
                descriptors = feature.getFeatureType().getAttributeDescriptors();
            }
            Map<String, Object> dataRow = new HashMap<String, Object>();
            for (int idx = 0; idx < descriptors.size(); idx++) {
                AttributeDescriptor desc = descriptors.get(idx);
                Object value = feature.getAttribute(desc.getName());
                dataRow.put(desc.getName().getLocalPart(), value);
                if (((desc.getName().getLocalPart().toLowerCase().contains("geom")) == true) && (value instanceof Point)) {
                    //POINT (2.159601897944868 41.36384684287495)
                    Point valueAsPoint = (Point) feature.getAttribute(desc.getName());
                    dataRow.put("lat", valueAsPoint.getY());
                    dataRow.put("lon", valueAsPoint.getX());
                }
            }
            data.put(String.valueOf(dataRow.get(featureIdField)), dataRow);
        }
        long tf = System.currentTimeMillis();
        log.info(MessageFormat.format(
                "All shape features loaded into the cache ({0}).", tf-t0));
    }
}
