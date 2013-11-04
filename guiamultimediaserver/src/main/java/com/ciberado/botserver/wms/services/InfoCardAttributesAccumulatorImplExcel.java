/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.datapublishing.InfoCard;
import com.ciberado.botserver.util.ExcelDumper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ciberado
 */
public class InfoCardAttributesAccumulatorImplExcel
implements InfoCardAttributesAccumulator{

    private String keyFieldName;

    private Map<String /* featureIdField */, Map<String /* FieldName */, String /* Value */>> data =
            new HashMap<String, Map<String, String>>();

    public InfoCardAttributesAccumulatorImplExcel(String fileName, String keyFieldName)
    throws IOException {
        super();
        this.keyFieldName = keyFieldName;
        ExcelDumper dumper = new ExcelDumper();
        dumper.bulkXlsToMap(fileName, keyFieldName, data);
    }


    @Override
    public void addAttributes(String key, InfoCard infoCard) {
        Map<String, String> rowData = this.data.get(key);
        if (rowData != null) for (Map.Entry<String, String> entry : rowData.entrySet()) {
            String value = entry.getValue().toString().replaceAll("[\\r\\n\"]", "    ");
            infoCard.set(entry.getKey(), value);
        }
    }

    public String getKeyFieldName() {
        return keyFieldName;
    }

    
}
