/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

import com.ciberado.botserver.datapublishing.InfoCard;
import com.ciberado.lang.IOUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ciberado
 */
public class TTSServlet extends HttpServlet {
    
    private WebApplicationContext springContext;
    private TTSGenerator generator;
        
    private File ttsFolder;
    
    @Override
    public void init() throws ServletException {
        this.springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        Properties configProps = (Properties) springContext.getBean("config");
        ttsFolder = new File(
                System.getProperty("java.io.tmpdir") + "/" +
                configProps.getProperty("tts.location")).getAbsoluteFile();    
        if (springContext.containsBean("ttsGenerator") == true) {
            this.generator = (TTSGenerator) springContext.getBean("ttsGenerator");
        }
    }



    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String ttsFile = request.getPathInfo().substring(1);  // example: 1822_es.mp3 o tilcor.mp3
        String path = ttsFolder.getAbsolutePath() + "/" + ttsFile;
        File cachedFile = new File(path);
        if (cachedFile.exists() == false) {
            String key;
            String language;
            int underscorePos = ttsFile.indexOf("_");
            if (underscorePos == -1) {
                key = ttsFile.substring(0, ttsFile.length() - ".mp3".length());
                language = "";
            } else {
                key = ttsFile.substring(0, underscorePos);
                language = ttsFile.substring(underscorePos+1, ttsFile.length() - ".mp3".length());
            }
            String description = this.retreiveDescription(language, key);
            if (description != null) {
                File newFile = generator.generateVoiceFile(language, description);
                newFile.renameTo(cachedFile);
            } else {
                cachedFile = new File(ttsFolder.getAbsolutePath() + "/voicedemo.mp3");
            }
        }
        response.setContentType("audio/mp3");
        response.setContentLength((int) cachedFile.length());
        BufferedInputStream in = null;
        OutputStream out = null;
        byte[] data = new byte[1024*128];
        try {
            in = new BufferedInputStream(new FileInputStream(cachedFile));        
            out = response.getOutputStream();
            int len;
            do {
                len = in.read(data, 0, data.length);
                if (len > 0) {
                    out.write(data, 0, len);
                }
            } while (len != -1);
         } finally {
            IOUtil.closeResources(new Object[] {out, in});
        }
    } 
    
    private String retreiveDescription(String language, String query) {
        throw new NotImplementedException("Previous imp used InfoCardService.");
    }    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>




}
