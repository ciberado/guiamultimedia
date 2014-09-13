package com.ciberado.botserver.tts;

import com.ciberado.lang.IOUtil;
import com.ciberado.lang.SystemException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ciberado
 */
public class TTSGeneratorImplFestival extends TTSGeneratorImpl {
    private static final Log log = LogFactory.getLog(TTSGeneratorImplFestival.class);

    public File generateVoiceFile(String language, String sentence) {
        File textFile = null;
        File wavFile = null;
        try {
            String fileName = generateFilame(sentence);
            File mp3File = new File(fileName);
            textFile = new File(fileName.replace("mp3", "txt"));
            FileUtils.writeStringToFile(textFile, sentence, "UTF-8");
            wavFile = new File(fileName.replace("mp3", "wav"));

            // "/usr/bin/text2wave -eval "(voice_upc_ca_pep_clunits)" -o /home/ciberado/.netbeans/6.9/apache-tomcat-6.0.26_base/temp/Segonsconstae_1298820270682.wav /home/ciberado/.netbeans/6.9/apache-tomcat-6.0.26_base/temp/Segonsconstae_1298820270682.txt"
            String cmdText2Wave = "/usr/bin/text2wave "
                                + textFile.getAbsolutePath() +  " "
                                //+ "-eval \"(voice_upc_ca_ona_hts)\" "
                                + "-o " + wavFile.getAbsolutePath() + " "
                                + "";
            //ProcessBuilder pbuilder = new ProcessBuilder(cmdText2Wave);
            //pbuilder.redirectErrorStream(true);
            Process procText2Wave = Runtime.getRuntime().exec(cmdText2Wave);

            InputStream is = procText2Wave.getErrorStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;


            while ((line = br.readLine()) != null) {
               System.out.println(line);
            }
            procText2Wave.waitFor();

            log.info("Executed tts: " + cmdText2Wave + ". Exit value: " + procText2Wave.exitValue());
            String cmdLame = "/usr/bin/lame " + wavFile.getAbsolutePath() +  " " + fileName;
            Process procLame = Runtime.getRuntime().exec(cmdLame);
            procLame.waitFor();
            
            return mp3File;
        } catch (InterruptedException ex) {
            throw new SystemException(ex);
        } catch (IOException ex) {
            throw new SystemException(ex);
        } finally {
            if (textFile != null) {
                textFile.delete();
            }
            if (wavFile != null) {
                wavFile.delete();
            }
        }
    }

    class DumperThread extends Thread {
        private InputStream in;
        private OutputStream out;
        private boolean active;

        public DumperThread(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            try {
                active = true;
                int len;
                byte[] data = new byte[1024*64];
                do {
                    len = in.read(data, 0, data.length);
                    if (len > 0)  {
                        out.write(data, 0, len);
                    }
                } while ((active == true) && (len != -1));
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                IOUtil.closeResources(new Object[] {in, out});
            }
        }

        public void done() {
            active = false;
        }


        public InputStream getIn() {
            return in;
        }

        public void setIn(InputStream in) {
            this.in = in;
        }

        public OutputStream getOut() {
            return out;
        }

        public void setOut(OutputStream out) {
            this.out = out;
        }


    }



}
