/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.Specimen;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author ciberado
 */
@Controller
@RequestMapping("/features")
public class FeatureInfoCtrl {

    @Inject
    private ObjectMapper mapper; 

    @Inject
    private FeatureInfoService featureInfoService;
    
    @Inject 
    private WikipediaService wikipediaService;
    
    @Inject
    private CacheManager cacheManager;
    
    private void writeResponse(PrintWriter out, ObjectMapper mapper, Object response, String callback) 
    throws IOException {
        if (callback != null) {
            out.write(callback + "(");
            out.flush();
        } 
        out.write(mapper.writeValueAsString(response));
        if (callback != null) {
            out.write(")");
            out.flush();
        }
    }
    
    @RequestMapping(value="/search", method= RequestMethod.GET)
    public void getFeaturesWithPattern(
            @RequestParam String pattern,
            @RequestParam double lat, @RequestParam double lon,
            @RequestParam(required = false) String callback,
            HttpServletResponse response) 
    throws IOException {       
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        List<Specimen> specimens = featureInfoService.getSpecimens(pattern, lat, lon);
        this.writeResponse(out, mapper, new LinkedHashSet(specimens), callback);        
    }

    @RequestMapping(value="/near", method= RequestMethod.GET)
    public void getFeaturesNearLatLon(
            @RequestParam double lat, @RequestParam double lon,
            @RequestParam(defaultValue="-1.0") double maxDistance,
            @RequestParam(required = false) String callback,
            HttpServletResponse response) 
    throws IOException {       
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        List<Specimen> specimens = featureInfoService.getSpecimens(lat, lon, maxDistance);
        this.writeResponse(out, mapper, new LinkedHashSet(specimens), callback);        
    }

    @RequestMapping(value="/bbox", method= RequestMethod.GET)
    public void getFeaturesReferencesInBBox(
            @RequestParam double minLat, @RequestParam double minLon,
            @RequestParam double maxLat, @RequestParam double maxLon,
            @RequestParam(required = false) String callback,
            HttpServletResponse response) 
    throws IOException {       
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        List<Object> references = featureInfoService.getSpecimenReferences(minLat, minLon, maxLat, maxLon);
        if (callback != null) {
            out.write(callback + "(");
        }
        out.write("[");
        MessageFormat mf = new MessageFormat("[{0,number,#.#######}, {1,number,#.######}, {2}]", Locale.ENGLISH);
        StringBuffer line = new StringBuffer();
        for (int idx=0; idx < references.size(); idx++) {
            Object[] rowArray = (Object[]) references.get(idx);
            Double lat = (Double) rowArray[0];
            Double lon = (Double) rowArray[1];
            String port = (String) rowArray[2];
            int portCode = getPortCodeFor(port);
            line.setLength(0);
            mf.format(new Object[] {lat, lon, portCode}, line, null);
            if (idx < references.size()-1) {
                line.append(",");
            }
            out.write(line.toString());
        }
        out.write("]");
        if (callback != null) {
            out.write(")");
        }
    }

    private int getPortCodeFor(String port) {
        int result = -1;
        if (port != null) port = port.toLowerCase();
        if ("herbàcia".equals(port) == true ) result = 1;
        else if ("bulbosa".equals(port) == true ) result = 2;
        else if ("matoll".equals(port) == true ) result = 3;
        else if ("arbust".equals(port) == true ) result = 4;
        else if ("arbust perenne".equals(port) == true ) result = 4;
        else if ("arbust caduc".equals(port) == true ) result = 5;
        else if ("arbre".equals(port) == true ) result = 6;
        else if ("arbre perenne".equals(port) == true ) result = 6;
        else if ("arbre caduc".equals(port) == true ) result = 7;
        else if ("liana".equals(port) == true ) result = 8;
        else if ("crassa".equals(port) == true ) result = 9;
        else if ("palmera".equals(port) == true ) result = 12;
        
        return result;
    }

    @RequestMapping(value="/wikipedia", method= RequestMethod.GET)
    public void getFeaturesDescriptionFromWikipedia(
            @RequestParam String wikipediaLink,
            @RequestParam(required = false) String callback,
            HttpServletResponse response) 
    throws IOException {       
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        if (callback != null) {
            out.write(callback + "(");
        }        
        StringWriter sw = new StringWriter(512);
        mapper.writeValue(sw, wikipediaService.getResume(wikipediaLink));
        sw.close();
        out.write(sw.getBuffer().toString());
        if (callback != null) {
            out.write(")");
        }
        out.flush();
    }
    
    @RequestMapping(value="/wikipedia/reset", method= RequestMethod.GET)
    public void resetWikipediaCache(HttpServletResponse response) throws IOException {
        cacheManager.getCache("wikipediaResumes").clear();
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.println("<h1>Cache reseteada</h1><p>Fecha servidor: " + new Date() + ".</p>");
    }
              
}
