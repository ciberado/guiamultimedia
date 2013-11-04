/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author ciberado
 */
public class DescriptionDataSourceImplXls implements DescriptionDataSource {
    private static final Log log = LogFactory.getLog(DescriptionDataSourceImplXls.class);

    private Map<String /* species name */, String /* Descripton */> data =
            new HashMap<String, String>();

    public DescriptionDataSourceImplXls(String xlsPathName)
    throws IOException {
        log.info("DescriptionDataSourceImplXls: " + xlsPathName);
        HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(xlsPathName));
        HSSFSheet sheet = book.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        for (int idx=1; idx < rows; idx++) {
            HSSFRow row = sheet.getRow(idx);
            if ((row.getCell(1) != null) && (row.getCell(21) != null)) {
                String species = row.getCell(1).getStringCellValue();
                String description = row.getCell(21).getStringCellValue();
                data.put(species.toLowerCase(), description);
            }
        }
    }

    public String getDescription(String language, String query) {
        return data.get(query.toLowerCase());
    }

}
