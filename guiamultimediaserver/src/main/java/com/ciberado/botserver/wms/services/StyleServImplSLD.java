/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.wms.services;

import com.ciberado.lang.SystemException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory;


/**
 *
 * @author ciberado
 */
public class StyleServImplSLD implements StyleServ {

    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    
    private String name;
    
    private String sourcePath;
    
    private long sourceFileDate;
    
    private Style style;
    

    public StyleServImplSLD() {
    }

    private void loadStyle() throws IOException {
        File sourceFile = new File(sourcePath);
        long currentDate = sourceFile.lastModified();
        if ((sourceFileDate == 0) || (currentDate != sourceFileDate)) {
            sourceFileDate= currentDate;
            //InputStream xml = StyleServImplSLD.class.getClassLoader().getResourceAsStream(sourcePath);
            InputStream xml = null;
            try {
                xml = new BufferedInputStream(new FileInputStream(sourceFile));
                SLDParser parser = new SLDParser(
                        CommonFactoryFinder.getStyleFactory(null), xml);
                Style[] styles = parser.readXML();
                this.style = styles[0];
            } finally {
                if (xml != null) {
                    xml.close();
                }
            }
        }
    }
    
    public Style getStyle() {
        try {
            loadStyle();
            return style;
        } catch (IOException exc) {
            throw new SystemException(exc);
        }        
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
    
    
    

}
