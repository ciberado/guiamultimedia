/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botmetadata;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author ciberado
 */
public class JdbcDataUpdater implements DataUpdater {

    
    private String url;
    private Connection connection;
    private Properties sqlSentences;
    
    private PreparedStatement stmtSelectDadesEstructurals;
    private PreparedStatement stmtUpdateMultimedia;
    private PreparedStatement stmtInsertMultimedia;
    private PreparedStatement stmtSelectSpeciesWithoutPhoto;



    public JdbcDataUpdater(String url) throws RuntimeException /* IOException */ {
        try {
            this.url = url;
            this.sqlSentences = new Properties();
            this.sqlSentences.load(DataUpdater.class.getClassLoader().getResourceAsStream("META-INF/sql.properties"));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    @Override
    public void begin() throws RuntimeException /* SQLException */{
        try {
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
            stmtSelectDadesEstructurals = connection.prepareStatement(
                    sqlSentences.getProperty("selectDadesEstructurals"));
            stmtUpdateMultimedia = connection.prepareStatement(
                    sqlSentences.getProperty("updateMultimedia"));
            stmtInsertMultimedia = connection.prepareStatement(
                    sqlSentences.getProperty("insertMultimedia"));
            stmtSelectSpeciesWithoutPhoto = connection.prepareStatement(
                    sqlSentences.getProperty("selectSpeciesWithoutPhoto"));
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }        
    }

    private int findDadesEstructuralsId(String speciesName) throws RuntimeException /*SQLException*/{
        ResultSet rs = null;
        try {
            stmtSelectDadesEstructurals.setString(1, speciesName);
            rs = stmtSelectDadesEstructurals.executeQuery();
            int id = -1;
            if (rs.next() == true) {
               id = rs.getInt("id");
            }
            return id;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                rs.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private boolean tryToUpdate(String fileName, int dadesEstructuralsId, 
                                String speciesName, String author, String regionType) 
    throws RuntimeException /* SQLException */{
        try {
            stmtUpdateMultimedia.setInt(1, dadesEstructuralsId);
            stmtUpdateMultimedia.setString(2, speciesName);
            stmtUpdateMultimedia.setString(3, author);
            stmtUpdateMultimedia.setString(4, regionType);
            stmtUpdateMultimedia.setString(5, fileName);
            int modifiedRows = stmtUpdateMultimedia.executeUpdate();
            return modifiedRows != 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void insertNewRow(String fileName, int dadesEstructuralsId, 
                              String speciesName, String author, String regionType) 
    throws RuntimeException /* SQLException */{
        try {
            stmtInsertMultimedia.setInt(1, dadesEstructuralsId);
            stmtInsertMultimedia.setString(2, speciesName);
            stmtInsertMultimedia.setString(3, fileName);
            stmtInsertMultimedia.setString(4, author);
            stmtInsertMultimedia.setString(5, regionType);
            int modifiedRows = stmtInsertMultimedia.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    
    @Override
    public void updateInfo(File originalFile, String speciesName, String authorName, String regionType) throws RuntimeException {
        int dadesEstructuralsId = findDadesEstructuralsId(speciesName);
        if (tryToUpdate(originalFile.getName(), dadesEstructuralsId, speciesName, authorName, regionType) == false) {
            insertNewRow(originalFile.getName(), dadesEstructuralsId, speciesName, authorName, regionType);
        }
    }

    @Override
    public void end(boolean ok) throws RuntimeException /* SQLException */{
        try {
            if (ok == true) {
                connection.commit();
            } else {
                connection.rollback();
            }
            connection.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
