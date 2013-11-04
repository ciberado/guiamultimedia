/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package botsqldumper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ciberado
 */
public class ImageDumper {

    public static void main(String[] args) throws Exception {
        
//        String url = "jdbc:postgresql://172.31.5.68/botanicdb";
//        Connection con = DriverManager.getConnection(url, "postgres", "141414");
        String url = "jdbc:postgresql://localhost/botanicdb";
        Connection con = DriverManager.getConnection(url, "postgres", "adminadmin");

        String sqlDelete = "DELETE FROM t_taxa_media";
        PreparedStatement stmtDelete = con.prepareStatement(sqlDelete);
        stmtDelete.executeUpdate();
        stmtDelete.close();
        
        String sqlSelect = "SELECT codi_esp FROM taxa WHERE genere=? and especie=?";
        PreparedStatement stmtSelect = con.prepareStatement(sqlSelect);

        String sqlInsert = "INSERT INTO t_taxa_media (codi_esp, ta_me_fitxer) VALUES (?, ?)";
        PreparedStatement stmtInsert = con.prepareStatement(sqlInsert);


        String folderPath = "C:/datos/Dropbox/ws/botguia/botserver-jardibotanic/guiamultimedia/imagesdb/interes";
        File folder = new File(folderPath);

        File[] files = folder.listFiles();
        List<File> filesNotFound = new ArrayList<File>();
        for (File currentFile : files) {
            if (currentFile.isFile() == true) {
                String namePrefix = currentFile.getName();
                System.out.println("Procesando " + namePrefix + ".");
                int pos = namePrefix.indexOf("_");
                if (pos == -1) pos = namePrefix.indexOf(".");
                namePrefix = namePrefix.substring(0, pos);
                String[] parts = namePrefix.split(" ");
                if (parts.length  < 2) {
                    System.out.println("Nombre erróneo.");
                    filesNotFound.add(currentFile);
                    continue;
                }
                String genus = parts[0].trim();
                String species = parts[1].trim();
                stmtSelect.setString(1, genus);
                stmtSelect.setString(2, species);
                ResultSet rs = stmtSelect.executeQuery();
                int count = 0;
                while (rs.next() == true) {
                    String codiEsp = rs.getString("codi_esp");
                    stmtInsert.setString(1, codiEsp);
                    stmtInsert.setString(2, "interes/" + currentFile.getName());
                    stmtInsert.executeUpdate();
                    System.out.println("Actualizado fichero " + currentFile);
                    count = count + 1;
                }
                if (count == 0) {
                    System.out.println("Registro para " + namePrefix + " no encontrado.");
                    filesNotFound.add(currentFile);
                }
            }
        }

        stmtInsert.close();
        stmtSelect.close();
        con.close();

        System.out.println();
        System.out.println("Encontrados: " + (files.length - filesNotFound.size()));
        System.out.println("No encontrados: " + filesNotFound.size());
        System.out.println("");
        for (File file : filesNotFound) {
            System.out.println(file);
        }
    }
}
