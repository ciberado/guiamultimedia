/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package botsqldumper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ciberado
 */
public class AudioDumper {
    public static void main(String[] args) throws Exception {
        
        //String url = "jdbc:postgresql://172.31.5.68/botanicdb";
        //Connection con = DriverManager.getConnection(url, "postgres", "141414");
        String url = "jdbc:postgresql://localhost/botanicdb";
        Connection con = DriverManager.getConnection(url, "postgres", "adminadmin");

        String sqlSelect = "SELECT codi_esp, ta_desc_idioma, ta_desc_text " +
                           "FROM t_taxa_desc " +
                           "WHERE ta_desc_text is not null "
                         + "  AND ta_desc_audio is null "
                         + "  AND ta_desc_idioma= ?"  ;
        PreparedStatement stmtSelect = con.prepareStatement(sqlSelect);
        stmtSelect.setString(1, "ca");
        
        String sqlUpdate = "UPDATE t_taxa_desc "
                         + "SET ta_desc_audio = ? "
                         + "WHERE codi_esp = ? AND ta_desc_idioma=? ";
        PreparedStatement stmtUpdate = con.prepareStatement(sqlUpdate);

        String folderPath = "C:/datos/Dropbox/ws/botguia/botserver-jardibotanic/guiamultimedia/audiodb/taxons";

        List<String> failedCodes = new ArrayList<String>();
        ResultSet rs = stmtSelect.executeQuery();
        
        StringBuilder script = new StringBuilder();
        
        while (rs.next() == true) {
            String code = rs.getString("codi_esp");
            String language = rs.getString("ta_desc_idioma");
            String text = rs.getString("ta_desc_text");
            if (text == null || text.isEmpty() == true) {
                continue;
            }
            String fileName = code + "_" + language;
            
            System.out.println("Procesando " + fileName + ".");
            File file = new File(folderPath + "/" + fileName  + ".txt");
            if (file.exists() == true) {
                file.delete();
            }
            try {
                FileUtils.writeStringToFile(file, text, "ISO-8859-15");

                stmtUpdate.setString(1, "taxons/" + fileName + ".mp3");
                stmtUpdate.setString(2, code);
                stmtUpdate.setString(3, language);
                stmtUpdate.execute();
                
                script.append(MessageFormat.format(
                        "text2wave {0} -o {1} -eval ''(language_catalan)''",
                        fileName + ".txt", fileName + ".wav"))
                      .append(" \n");
                script.append(MessageFormat.format(
                        "lame -m m -v -V9 {0} {1}",
                        fileName + ".wav", fileName + ".mp3"
                        ))
                      .append(" \n");
                script.append(MessageFormat.format(
                        "oggenc {0} -o {1} ",
                        fileName + ".wav", fileName + ".ogg"
                        ))
                      .append(" \n");
            } catch (IOException exc) {
                System.out.println("Error: " + exc.getMessage());
                failedCodes.add(code);
            } 
        }
        
        con.close();
        
        System.out.println("\r\nErrores:");
        for (String code : failedCodes) {
            System.out.println(code);
        }
        
        System.out.println("Generando script.");
        File scriptFile = new File(folderPath + "/script-tts.sh");
        FileUtils.writeStringToFile(scriptFile, script.toString(), "UTF-8");
        
        System.out.println("Fin.");
    }
    
}
