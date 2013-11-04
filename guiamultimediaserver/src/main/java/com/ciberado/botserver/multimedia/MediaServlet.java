/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.multimedia;

import com.ciberado.lang.SystemException;
import java.io.*;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author ciberado
 */
public class MediaServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(MediaServlet.class);
    private WebApplicationContext springContext;
    private Set<File> mediaFolders = new LinkedHashSet<File>();

    @Override
    public void init() throws ServletException {
        this.springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        Properties configProps = (Properties) springContext.getBean("config");
        String[] mediaFoldersPaths = configProps.getProperty("media.location").split(",");
        log.info("Initialization of MediaServlet. Locations = " + configProps.getProperty("media.location"));
        for (String folder : mediaFoldersPaths) {
            this.mediaFolders.add(new File(folder.trim()));
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String resource = request.getParameter("resource");
        if (resource.endsWith("jpg") == true) {
            response.setContentType("image/jpeg");
        } else if (resource.endsWith("mp3") == true) {
            response.setContentType("audio/mp3");
            response.setHeader("Content-Disposition", "inline; filename=" + resource);
        } else if (resource.endsWith("ogg") == true) {
            response.setContentType("audio/ogg");
            response.setHeader("Content-Disposition", "inline; filename=" + resource);
        } else {
            throw new SystemException("Resource type not supported: " + resource);
        }
        File foundFile = null;
        File currentFile = null;
        log.debug("Searching resource " + resource + ".");
        for (File currentFolder : mediaFolders) {
            currentFile = new File(currentFolder.getAbsolutePath() + "/" + resource);
            if (currentFile.exists() == true) {
                foundFile = currentFile;
                break;
            } else {
                log.debug("File " + currentFile + " not found.");
            }
        }
        if (foundFile == null) {
            throw new FileNotFoundException(resource + " not found ("+currentFile.getAbsolutePath()+".");
        }
        byte[] data = new byte[1024 * 128];
        response.setContentLength((int) foundFile.length());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(foundFile));
        OutputStream out = response.getOutputStream();
        int len;
        do {
            len = in.read(data, 0, data.length);
            if (len > 0) {
                out.write(data, 0, len);
            }
        } while (len != -1);
        in.close();
        out.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
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
     * Handles the HTTP
     * <code>POST</code> method.
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
