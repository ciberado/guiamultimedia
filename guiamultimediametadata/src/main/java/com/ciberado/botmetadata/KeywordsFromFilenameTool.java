/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botmetadata;

import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ciberado
 */
public class KeywordsFromFilenameTool {
    private static final Log log = LogFactory.getLog(KeywordsFromFilenameTool.class);
    private static final Pattern spPattern =
            Pattern.compile("(\\D*)\\d*\\.(jpg|nef)");
    
    
    private List<DataUpdater> dataUpdaters = new ArrayList<>();
    
    private File folder;
    private String authorName;
    
    public KeywordsFromFilenameTool() {        
    }

    public List<DataUpdater> getDataUpdaters() {
        return dataUpdaters;
    }

    public void setDataUpdaters(List<DataUpdater> dataUpdaters) {
        this.dataUpdaters = dataUpdaters;
    }
    
    private String[] extractPartsFromFilename(String fileName) {
        fileName = fileName.replaceAll("\\(|\\)", "_");
        StringBuffer sb = new StringBuffer();
        for (int idx=0; idx < fileName.length(); idx++) {
            char chr = fileName.charAt(idx);
            if (Character.isLetter(chr) || Character.isWhitespace(chr)) {
                sb.append(fileName.charAt(idx));
            } else if (chr == '.') {
                break;
            }
        }
        fileName = sb.toString();
        String[] parts = fileName.split("_");
        
        return parts;
    }
    
    public void execute() {
        if (authorName == null) {
            authorName = "unknown";
        } 
        
        File[] fileList = folder.listFiles(new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg") 
                        ||name.toLowerCase().endsWith(".nef");
            }
        });

        int skipped = 0;
        for (File file : fileList) {
            System.out.println(MessageFormat.format("Updating {0}.", file));
            try {
                String[] parts = this.extractPartsFromFilename(file.getName());
                if ((parts.length != 1) && (parts.length != 2)) {
                    log.warn(
                       MessageFormat.format("Incorrect filename : {0}.", file));
                } else {
                    String speciesName = StringUtils.capitalize(parts[0].toLowerCase()).trim();
                    String[] keywords = {"sp:" + speciesName,
                                         "author:" + authorName};
                    for (DataUpdater updater : this.dataUpdaters) {
                        updater.updateInfo(file, speciesName, authorName, "regionType");
                    }
                }
            } catch (RuntimeException exc) {
                System.out.println(MessageFormat.format("Error processing {0}.", file));
                skipped = skipped + 1;
            }
        }
        
        System.out.println(MessageFormat.format("Done. Processed {0} files. Skipped {1} files", 
                fileList.length - skipped, skipped));
    }
    
    private static Options buildOptions() {
        Option folderOpt  = OptionBuilder
                                .withArgName("path")
                                .hasArg()
                                .withDescription("folder to be processed" )
                                .create("folder" );        
        Option authorOpt = OptionBuilder
                                .withArgName("name")
                                .hasArg()
                                .withDescription("author of the images" )
                                .create("author" );        
        Options options = new Options();
        options.addOption(folderOpt);
        options.addOption(authorOpt);
        return options;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public static void main(String[] args)   {
        org.apache.log4j.BasicConfigurator.configure();
        Options options = buildOptions();
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse( options, args );
            File folder = line.hasOption("folder") ?
                          new File(line.getOptionValue("folder")) :
                          new File(".");
            if ((folder.exists() == false) || (folder.isDirectory()==false)) {
                System.out.println(MessageFormat.format("Folder {0} is not acceptable.", 
                                   folder.getAbsolutePath()));
                System.exit(-1);
            }
            
            List<DataUpdater> dataUpdaters = new ArrayList<>();
            dataUpdaters.add(new IPTCKeywordsDataUpdater());
            
            if (line.hasOption("dburl") == true) {
                String databaseURL = line.getOptionValue("dburl");
                dataUpdaters.add(new JdbcDataUpdater(databaseURL));                
            }
            
            KeywordsFromFilenameTool tool = new KeywordsFromFilenameTool();
            tool.setFolder(folder);
            tool.setAuthorName(line.getOptionValue("author"));
            tool.setDataUpdaters(dataUpdaters);
            tool.execute();
        } catch (ParseException exc) {
            System.out.println(
               MessageFormat.format("Error parsing arguments {0}.", exc));
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "KeywordsFromFilenameTool", options);
            System.exit(-1);
        }        
        
    }
    
}
