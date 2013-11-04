/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

import com.ciberado.lang.IOUtil;
import com.ciberado.lang.SystemException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author ciberado
 */
public class DescriptionDataSourceImplWikipedia implements DescriptionDataSource {

    public String getDescription(String language, String query) {
        DefaultHttpClient httpclient = null;
        InputStream in = null;
        try {
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("go", query));
            URI uri = URIUtils.createURI("http", language + ".mobile.wikipedia.org", -1, "/transcode.php",
                                         URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpget = new HttpGet(uri);
            httpclient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpResponse response = httpclient.execute(httpget, localContext);
            String desc = EntityUtils.toString(response.getEntity(), "UTF-8");
            return desc.toLowerCase()
                       .substring(desc.indexOf("<body>") + "<body>".length())
                       .replaceAll("\\<.*?>","")
                       .replaceAll("á","a")
                       .replaceAll("é","e")
                       .replaceAll("í","i")
                       .replaceAll("ó","o")
                       .replaceAll("ú","u")
                       .replaceAll("\\.",". ")
                       .replaceAll("\\s+"," ")
                       .replaceAll("[^\\w\\.,@-]"," ");
        } catch (Exception ex) {
            throw new SystemException(ex);
        } finally {
            IOUtil.closeResources(in);
            httpclient.getConnectionManager().shutdown();
        }
    }

}
