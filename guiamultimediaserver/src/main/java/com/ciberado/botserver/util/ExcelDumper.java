/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *
 * @author ciberado
 */
public class ExcelDumper {

    
    public void bulkXlsToMap(String fileName, String featureIdField,
                              Map<String /* featureIdField */, Map<String /* FieldID */, String /* Value */>> data)
    throws IOException {
        HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(fileName));
        HSSFSheet sheet = book.getSheetAt(0);
        int rows = sheet.getPhysicalNumberOfRows();
        int keyIndex = 0;
        HSSFRow row = sheet.getRow(0);
        int columnNumber = row.getPhysicalNumberOfCells();
        for (int idxColumn = 0; idxColumn <= columnNumber; idxColumn++) {
            HSSFCell cell = row.getCell(idxColumn);
            if (cell != null) {
                String value = cell.getStringCellValue();
                if (value.equalsIgnoreCase(featureIdField) == true) {
                    keyIndex = idxColumn;
                    break;
                }
            }
        }
        for (int idxRow = 1; idxRow < rows; idxRow++) {
            row = sheet.getRow(idxRow);
            if (row != null) {
                Map rowData = new HashMap<String, String>();
                HSSFCell idCell = row.getCell(keyIndex);
                if (idCell != null) {
                    String id = idCell.getStringCellValue();
                    for (int idxColumn = 1; idxColumn <= columnNumber; idxColumn++) {
                        HSSFCell captionCell = sheet.getRow(0).getCell(idxColumn);
                        HSSFCell dataCell = row.getCell(idxColumn);
                        if ((captionCell != null) && (dataCell != null)) {
                            String key = captionCell.getStringCellValue();
                            String value = (dataCell.getCellType() == HSSFCell.CELL_TYPE_STRING) ?
                                           dataCell.getStringCellValue() : "";
                            if (key.isEmpty() == false) {
                                rowData.put(key, value);
                            }
                        }
                    }

                    data.put(id, rowData);
                }
            }
        }

    }

}
