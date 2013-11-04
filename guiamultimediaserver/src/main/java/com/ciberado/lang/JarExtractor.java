/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.lang;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import static java.text.MessageFormat.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ciberado
 */


public class JarExtractor {
    private static final Logger log = LoggerFactory.getLogger(JarExtractor.class);
    
    public JarExtractor(Object containedInJarObject, String destDir) 
    throws IOException{
        String jarPath = this.getPath(containedInJarObject.getClass());
        jarPath = jarPath.substring("file:".length());
        if (destDir.endsWith("/") == true) {
            destDir = destDir.substring(0, destDir.length()-1);
        }
        log.info(format("eXtracting jar contents from {0} into {1}.", jarPath, destDir));
        extractJar(jarPath, destDir);
    }

    
    private void extractJar(String jarFile, String destDir) throws IOException {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enumeration = jar.entries();
        while (enumeration.hasMoreElements()) {
            java.util.jar.JarEntry jarEntry = 
                    (java.util.jar.JarEntry) enumeration.nextElement();
            java.io.File destinationFile = 
                    new java.io.File(destDir + java.io.File.separator + jarEntry.getName());
            if (jarEntry.isDirectory()) { 
                log.debug(format("Creating dir {0}.", jarEntry));
                destinationFile.mkdirs();
            } else {
                if (destinationFile.exists() && jarEntry.getTime() != destinationFile.lastModified()) {
                    log.debug(format("Removing old version of file {0}.", jarEntry));  
                    destinationFile.delete();
                }                    
                if (destinationFile.exists() == true) {
                    log.debug(format("File {0} already exists.", jarEntry));  
                } else {
                    log.debug(format("eXtrating file {0}.", jarEntry));
                    InputStream is = jar.getInputStream(jarEntry);
                    OutputStream fos = 
                            new BufferedOutputStream(
                                new FileOutputStream(destinationFile));
                    org.apache.commons.io.IOUtils.copy(is, fos);
                    fos.close();
                    is.close();
                    destinationFile.setLastModified(jarEntry.getTime());
                }
            }
        }        
    }
    
    private String getPath(Class cls) {
        String cn = cls.getName();
        String rn = cn.replace('.', '/') + ".class";
        String path =
                getClass().getClassLoader().getResource(rn).getPath();
        int ix = path.indexOf("!");
        if (ix >= 0) {
            return path.substring(0, ix);
        } else {
            return path;
        }
    }
    
    
}
