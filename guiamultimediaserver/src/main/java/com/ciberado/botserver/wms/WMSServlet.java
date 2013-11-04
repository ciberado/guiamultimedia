package com.ciberado.botserver.wms;

import com.ciberado.botserver.wms.services.CapabilitiesServ;
import com.ciberado.botserver.wms.services.ImageMapServ;
import com.ciberado.lang.IOUtil;
import com.ciberado.lang.SystemException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
  
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 */
public class WMSServlet extends javax.servlet.http.HttpServlet {

    static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WMSServlet.class);
    private WebApplicationContext springContext;
    /** Cach� con las capabilities convertidas en text/xml. */
    private Document capabilities = null;
    /** Identificador del servlet. Permite seguir su utilizaci�n en la auditor�a. */
    private String servletId;
    private CapabilitiesServ capabilitiesService;
    private ImageMapServ imageMapService;
    private File tmpFolder;

    /**
     * http://127.0.0.1:8084/botserver/wms?request=GetMap&SRS=EPSG:23031&BBOX=429424.48,4579227.98,429954.296,4579822.794&width=1024&height=1024&styles=default&format=image/png&transparent=true&layers=Fitoepisodis
     * http://127.0.0.1:8080/botserver/wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&LAYERS=fitoepisodis&STYLES=default&FORMAT=image/png&BGCOLOR=0xFFFFFF&TRANSPARENT=FALSE&SRS=EPSG:4326&BBOX=2.1550958557555266,41.359636629839656,2.161404296976646,41.365012722558106&WIDTH=512&HEIGHT=512&REASPECT=false
     */
    public void init(ServletConfig config) throws ServletException {
        try {
            super.init(config);
            this.springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
            Properties configProps = (Properties) springContext.getBean("config");
            servletId = config.getServletName().toLowerCase();
            log.info("Inicializando " + servletId + ".");

            capabilitiesService = (CapabilitiesServ) springContext.getBean("capabilitiesService");
            imageMapService = (ImageMapServ) springContext.getBean("imageMapService");
            tmpFolder = new File(
                    System.getProperty("java.io.tmpdir")
                    + "/" + configProps.getProperty("temp.folder")).getAbsoluteFile();
            if (tmpFolder.exists() == false) {
                tmpFolder.mkdirs();
            } else {
                FileUtils.cleanDirectory(tmpFolder);
            }

            this.processGetCapabilities(null, null, null);
        } catch (Exception e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    /** Invoca la funcionalidad del CtrlGetCapabilities y serializa a xml el documento
     *  obtenido. Cachea en this.capabilities el resultado antes de mandarlo a cliente.
     *  Tambi�n sustituye el tag #address# en el documento xml por la direcci�n del
     *  servicio.
     *
     * @param wmsRequest Petici�n wms con los datos de versi�n.
     * @param httpRequest Petici�n http.
     * @param httpResponse Respuesta http.
     */
    private void processGetCapabilities(WMSGetCapabilitiesRequest wmsRequest,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
            ServletException {
        try {
            if (capabilities == null) {
                File cache = new File(tmpFolder.getAbsolutePath()
                        + "/" + this.servletId + ".cap." + System.currentTimeMillis() + ".xml");
                if (cache.exists() == true) {
                    SAXBuilder builder = new SAXBuilder();
                    capabilities = builder.build(cache);
                } else {
                    this.capabilities = capabilitiesService.getCapabilities();
                    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
                    PrintStream out = null;
                    try {
                        out = new PrintStream(cache);
                        outputter.output(capabilities, out);
                    } finally {
                        IOUtil.closeResources(out);
                    }
                }
            }
            if (httpResponse != null) {
                httpResponse.setContentType("text/xml");
                httpResponse.setCharacterEncoding("UTF-8");
                Format format = Format.getPrettyFormat();
                XMLOutputter outputter = new XMLOutputter(format);
                outputter.output(this.capabilities, httpResponse.getWriter());
            }
        } catch (JDOMException e) {
            throw new SystemException(e);
        }
    }

    /** Implementa la funcionalidad del GetFeatureInfo.
     *
     * @param request Petici�n wms.
     * @param response Respuesta http.
     *
     * @throws IOException
     * @throws ServletException
     */
    private void processGetFeatureInfo(WMSGetFeatureInfoRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        throw new NotImplementedException();
    }

    private String descWmsGetMapRequest(WMSGetMapRequest req) {
        StringBuilder builder = new StringBuilder();
        for (WMSGetMapRequestLayer layer : req.getLayers()) {
            builder.append(layer.getName()).append("_");
            builder.append(layer.getStyle()).append("_");
        }

        if (req.getTileZoom() == 0) {
            builder.append(req.getBox().getMinx()).append("_").append(req.getBox().getMaxx()).append("_").append(req.getBox().getMiny()).append("_").append(req.getBox().getMaxy());
        } else {
            builder.append(req.getTileZoom()).append("_").append(req.getTileX()).append("_").append(req.getTileY()).append("_").append(req.getLayerNames());
        }


//        builder.append(req.getWidth()).append("_")
//               .append(req.getHeight()).append("_")
//               .append(req.getBox().getMinx()).append("_")
//               .append(req.getBox().getMaxx()).append("_")
//               .append(req.getBox().getMiny()).append("_")
//               .append(req.getBox().getMaxy()).append("_")
//               .append(req.getLayerNames());

        return builder.toString();
    }

    private void processGetMap(WMSGetMapRequest wmsGetMapRequest, HttpServletResponse response)
            throws IOException {
        BufferedInputStream cacheInputStream = null;
        try {
            String formatExtension = wmsGetMapRequest.getFormat();
            formatExtension = formatExtension.substring(formatExtension.lastIndexOf('/') + 1);
            File cacheFile = new File(tmpFolder.getAbsolutePath() + "/"
                    + this.servletId + ".map." + descWmsGetMapRequest(wmsGetMapRequest) + "." + formatExtension);
            if ((cacheFile.exists() == false) || (wmsGetMapRequest.isCacheable() == false)) {
                cacheFile.delete();
                log.info("Creating new tile for " + cacheFile.getName() + ".");
                BufferedOutputStream cacheOutputStream = new BufferedOutputStream(new FileOutputStream(cacheFile));
                this.imageMapService.processGetMap(wmsGetMapRequest, cacheOutputStream);
                cacheOutputStream.close();
                log.info("Tile size: " + cacheFile.length());
            } else {
                log.debug("Reusing tile for " + cacheFile.getName() + ".");
            }
            response.setContentType(wmsGetMapRequest.getFormat());
            response.setContentLength((int) cacheFile.length());
            cacheInputStream = new BufferedInputStream(new FileInputStream(cacheFile));
            OutputStream browserOut = response.getOutputStream();
            IOUtils.copy(cacheInputStream, browserOut);
            cacheInputStream.close();
            response.getOutputStream().flush();
        } finally {
            IOUtil.closeResources(cacheInputStream);
        }

    }

    private void processGetData(WMSGetDataRequest wmsGetDataRequest, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Obtiene la WMSRequest correspondiente a la petici�n http con los par�metros
     *  debidamente asignados, realiza la auditor�a de la petici�n y encamina el flujo
     *  hacia la rutina que responder� a cada una de las funcionalidades.
     *
     * @todo Averigua por qu� creamos bloqueos!!!! VIP.
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String query = request.getRequestURL().toString() + "?" + request.getQueryString();
        log.debug(query);

        WMSRequest wmsRequest = WMSRequest.getInstance(request);
        if (wmsRequest instanceof WMSGetCapabilitiesRequest) {
            this.processGetCapabilities((WMSGetCapabilitiesRequest) wmsRequest,
                    request, response);
        } else if (wmsRequest instanceof WMSGetMapRequest) {
            this.processGetMap((WMSGetMapRequest) wmsRequest, response);
        } else if (wmsRequest instanceof WMSGetFeatureInfoRequest) {
            this.processGetFeatureInfo((WMSGetFeatureInfoRequest) wmsRequest, response);
        } else if (wmsRequest instanceof WMSGetDataRequest) {
            this.processGetData((WMSGetDataRequest) wmsRequest, response);
        }
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        this.processRequest(request, response);
    }
}