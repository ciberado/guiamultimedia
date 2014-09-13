package com.ciberado.botserver.tts;

import com.ciberado.botserver.util.ExcelDumper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ciberado
 */
public class TTSBatch {

    private static final String  DESCRIPTION_FIELD = "DESCRIPCIO";
    
    private String descriptionsFilePath;
    private String destinationFolder;
    
    private String language;
    private String voice;

    private TTSGenerator ttsGenerator;
    private Map<String, Map<String, String>> data = new HashMap<String, Map<String, String>>();
    
    public TTSBatch(String descriptionsFilePath, String destinationFolder, 
                    String language, String voice) {
        this.descriptionsFilePath = descriptionsFilePath;
        this.destinationFolder = destinationFolder;
        this.language = language;
        this.voice = voice;
        this.ttsGenerator = new TTSGeneratorImplFestival();        
    }
    
    public void process() throws IOException {
        new ExcelDumper().bulkXlsToMap(descriptionsFilePath, "CODI_ESP", data);
        Iterator<String> featuresId = data.keySet().iterator();
        while (featuresId.hasNext() == true) {
            String featureId = featuresId.next();
            String description = data.get(featureId).get(DESCRIPTION_FIELD);
            File mp3File = ttsGenerator.generateVoiceFile(language, description);
            File destinationFile = new File(destinationFolder + "/" + featureId + ".mp3");
            FileUtils.moveFile(mp3File, destinationFile);
        }
    }
    
    public static void main(String[] args) throws IOException {
        TTSBatch batch = new TTSBatch(
                "/home/ciberado/ws/botguia/botserver/web/META-INF/data/descriptions.xls", 
                "/home/ciberado/ws/botguia/botserver/web/tts", "catalan", "upc_ca_ona_hts");
        batch.process();
    }
}
