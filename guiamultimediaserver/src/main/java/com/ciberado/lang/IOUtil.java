/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.lang;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author ciberado
 */
public class IOUtil {

    public static SystemException closeResources(Object resource) {
        return closeResources(new Object[] {resource});
    }

    public static SystemException closeResources(Object[] resources) {
        SystemException error = null;
        try {
            for (int idx=0; idx < resources.length; idx++) {
                Object resource = resources[idx];
                if (resource != null) {
                    Method closeMethod = resource.getClass().getMethod("close", new Class[]{});
                    if (closeMethod != null) {
                        try {
                            closeMethod.invoke(resource, (Object) null);
                        } catch (Exception e) {
                            error = null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
        return error;
    }

    public static String convertToRelativePath(String path, String relativeTo) {
        StringBuffer relativePath = null;
        
        // Thanks to:
        // http://mrpmorris.blogspot.com/2007/05/convert-absolute-path-to-relative-path.html
        path = StringUtils.replace(path, "\\", "/");
        relativeTo = StringUtils.replace(relativeTo, "\\", "/");
        
        if ((relativeTo.startsWith(".") == false) && (relativeTo.startsWith("/") == false)) {
            relativePath = new StringBuffer(relativeTo);
        } else if (path.equals(relativeTo) == false) {
            String[] absoluteDirectories = StringUtils.split(path, "/");
            String[] relativeDirectories = StringUtils.split(relativeTo, "/");

            //Get the shortest of the two paths
            int length = absoluteDirectories.length < relativeDirectories.length ? 
                absoluteDirectories.length : relativeDirectories.length;

            //Use to determine where in the loop we exited
            int lastCommonRoot = -1;
            int index;

            //Find common root
            for (index = 0; index < length; index++) {
                if (absoluteDirectories[index].equals(relativeDirectories[index])) {
                    lastCommonRoot = index;
                } else {
                    break;
                //If we didn't find a common prefix then throw
                }
            }
            if (lastCommonRoot != -1) {
                //Build up the relative path
                relativePath = new StringBuffer();
                //Add on the ..
                for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++) {
                    if (absoluteDirectories[index].length() > 0) {
                        relativePath.append("../");
                    }
                }
                for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++) {
                    relativePath.append(relativeDirectories[index] + "/");
                }
                relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
            }
        }        
        return relativePath == null ? null : relativePath.toString().trim();
    }

    private static final Map cache = new HashMap();
    public static String findPath(File rootFolder, String exeName) {
        String path = (String) cache.get(exeName);
        if (path == null) {
            File[] files = rootFolder.listFiles();
            if (files != null)  {
                for (int idx = 0; ((path == null) && (idx < files.length)); idx++) {
                    File current = files[idx];
                    if ((current.isFile() == true) && (current.getAbsolutePath().endsWith(exeName))) {
                        path = current.getParent();
                    }
                }
                for (int idx = 0; ((path == null) && (idx < files.length)); idx++) {
                    File current = files[idx];
                    if (current.isDirectory() == true) {
                        path = findPath(current, exeName);
                    }
                }
                if (path != null) {
                    cache.put(exeName, path);
                }
            }
        } 
        return path;
    }
    
    public static URL getClassLocation (final Class cls)  {
        if (cls == null) throw new IllegalArgumentException ("null input: cls");
        
        URL result = null;
        final String clsAsResource = cls.getName ().replace ('.', '/').concat (".class");
        
        final ProtectionDomain pd = cls.getProtectionDomain ();
        if (pd != null) 
        {
            final CodeSource cs = pd.getCodeSource ();
            if (cs != null) result = cs.getLocation ();
            
            if (result != null)
            {
                if ("file".equals (result.getProtocol ()))
                {
                    try
                    {
                        if (result.toExternalForm ().endsWith (".jar") ||
                            result.toExternalForm ().endsWith (".zip")) 
                            result = new URL ("jar:".concat (result.toExternalForm ())
                                .concat("!/").concat (clsAsResource));
                        else if (new File (result.getFile ()).isDirectory ())
                            result = new URL (result, clsAsResource);
                    }
                    catch (MalformedURLException ignore) {}
                }
            }
        }
        
        if (result == null)
        {
            final ClassLoader clsLoader = cls.getClassLoader ();
            
            result = clsLoader != null ?
                clsLoader.getResource (clsAsResource) :
                ClassLoader.getSystemResource (clsAsResource);
        }
        
        return result;
    }

    public static String convertToAbsolutePath(String fileName) {
        StringBuffer normalizedName = new StringBuffer(StringUtils.replace(fileName, "\\", "/"));
        int posDotDot = -1;
        do {
            posDotDot = normalizedName.indexOf("/..");
            if (posDotDot != -1) {
                int posPrevFolder = posDotDot-1;
                while ((posPrevFolder > 0) && (normalizedName.charAt(posPrevFolder) != '/')) {
                    posPrevFolder = posPrevFolder - 1;
                }
                if (posPrevFolder != -1) {
                    normalizedName.delete(posPrevFolder, posDotDot + "../".length());
                }
            }
        } while (posDotDot != -1);
        return normalizedName.toString();
    }

}
