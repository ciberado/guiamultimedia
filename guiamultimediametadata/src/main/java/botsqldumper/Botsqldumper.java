/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package botsqldumper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author ciberado
 */
public class Botsqldumper {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        String url = "jdbc:mysql://localhost/botanicdb";
        Connection con = DriverManager.getConnection(url, "root", "adminadmin");
        con.setAutoCommit(false);
        Statement stmt = con.createStatement();
        
        BufferedReader in = new BufferedReader(
                new FileReader("C:/Users/ciberado/Documents/dump2.sql"));
        
        StringBuilder sb = new StringBuilder();        
        String line;
        do {
            
            line = in.readLine();
            if (line != null) {
                sb.append(line).append("\r\n");
                if (line.endsWith(";") == true) {
                    //System.out.print(sb);
                    try {
                        stmt.executeUpdate(sb.toString());
                    } catch (SQLException exc) {
                        System.out.println("ERROR: " + sb);
                        System.out.println(exc);
                    }
                    sb.setLength(0);
                }
            }
        } while (line != null);
        
        in.close();
        con.commit();
        con.close();
        
    }
}





