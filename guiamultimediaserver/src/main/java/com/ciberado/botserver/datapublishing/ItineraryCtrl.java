/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.datapublishing.dto.ItineraryDTO;
import com.ciberado.botserver.model.Itinerary;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author ciberado
 */
@Controller
@RequestMapping("/itineraries")
public class ItineraryCtrl {

    @Inject
    private ItineraryService itineraryService;
    
    @Inject
    private ObjectMapper mapper; 

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
    
    @RequestMapping("/{id}")
    public void getItinerary(@PathVariable("id") String itineraryId,
                             Locale userLocale,
                             HttpServletRequest request,
                             HttpServletResponse response) 
    throws IOException {       
        // @TODO: fija el userlocale
        userLocale = new Locale("ca", "ES");
        Itinerary itinerary = 
                itineraryService.getItinerary(itineraryId);
        ItineraryDTO itineraryDTO = new ItineraryDTO(itinerary, userLocale, true);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json;charset=iso-8859-1");
        writeResponse(out, mapper, itineraryDTO, request.getParameter("callback"));
        
    }

    @RequestMapping("/")
    public void getItinerary(HttpServletRequest request,
                             HttpServletResponse response, 
                             Locale userLocale) 
    throws IOException {        
        // @TODO: fija el userlocale
        userLocale = new Locale("ca", "ES");
        Set<Itinerary> itineraries = 
                itineraryService.getItineraryReferences();
        Set<ItineraryDTO> references = 
                new LinkedHashSet<ItineraryDTO>();
        for (Itinerary itinerary : itineraries) {
            references.add(new ItineraryDTO(itinerary, userLocale));
        }
        response.setContentType("application/json;charset=iso-8859-1");
        PrintWriter out = response.getWriter();
        writeResponse(out, mapper, references, request.getParameter("callback"));
        
    }

   

}
