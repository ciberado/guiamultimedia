/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.tts;

import com.ciberado.lang.IOUtil;
import com.ciberado.lang.SystemException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ciberado
 */
public class TTSGeneratorImplGoogle extends TTSGeneratorImpl  {

    private static final Log log = LogFactory.getLog(TTSGeneratorImplGoogle.class);
    
    private static final int MIN_LENGTH = 40;
    
    private static final int MAX_LENGTH = 100;

    public TTSGeneratorImplGoogle() {
    }

    public File generateVoiceFile(String language, String text) {
        String fileName = generateFilame(text);
        File file = new File(fileName);
        OutputStream out = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            int from = 0;
            do {
                int to = from + MAX_LENGTH  > text.length() ? text.length() : from + MAX_LENGTH;
                if (to < text.length()) {
                    while ((to - from > MIN_LENGTH) && ((text.charAt(to)) != '.')) {
                        to = to-1;
                    }
                    while ((to > 1) && (text.charAt(to) != '.') && (Character.isWhitespace(text.charAt(to)) == false)) {
                        to = to-1;
                    }
                }
                invokeTTS(out, language, text.substring(from, to).trim() + " ");
                from = to + 1;
            }  while (from < text.length());
        } catch (IOException e) {
            e.printStackTrace();
            throw new SystemException(e);
        } finally {
            IOUtil.closeResources(out);
        }
        
        log.info("TTS file: " + file.getAbsolutePath() + ", " + file.length() + " bytes.");
        
        return file;
    }

    private void invokeTTS(OutputStream out, String language, String sentence) throws IOException {        
        InputStream in = null;
        try {
//            log.debug("Sentence: " + sentence);
//            NVPair formData[] = new NVPair[2]; 
//            formData[0] = new NVPair("tl", language);
//            formData[1] = new NVPair("q",  sentence);
//            NVPair headers[] = new NVPair[1];
//            headers[0] = new NVPair("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
//            HTTPConnection con = new HTTPConnection("translate.google.com");
//            con.setAllowUserInteraction(false);
//            HTTPResponse   rsp = con.Get("/translate_tts", formData, headers);
//            
//            in = new BufferedInputStream(rsp.getInputStream());
//            byte[] data = new byte[1024*128];
//            int len;
//            do {
//                len = in.read(data, 0, data.length);
//                if (len > 0) {
//                    out.write(data, 0, len);
//                    log.debug(len + " bytes written.");
//                }
//            } while (len != -1);
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SystemException(ex); //lacosaaaaaaaaaaaaa
        } finally {
            IOUtil.closeResources(in);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }

    

    public static void main(String[] args) throws Exception {
        TTSGenerator ttsGen = new TTSGeneratorImplGoogle();
        String desc =    "Esta rosa se conoce como agavanzo o escaramujo. "
                + "Es un arbusto espinoso y perenne de la familia de las ros�ceas. "
                + "Es originario de Europa, el noroeste de �frica y Asia occidental. "
                + "Actualmente su distribuci�n es bastante amplia.";
        File file = ttsGen.generateVoiceFile("es", desc);
        System.out.println(file.getAbsoluteFile());
        //File file = ttsGen.generateVoiceFile("ca", "hola bon dia, espero que tot estigui correctament desenvolupat i que aquest programa funcione per molt anys mes. gracies a tots.");
        Runtime.getRuntime().exec("c:/Program Files (x86)/VideoLAN/vlc/vlc.exe " + file.getAbsolutePath());
    }
}
