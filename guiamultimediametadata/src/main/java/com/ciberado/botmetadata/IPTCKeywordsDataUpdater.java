/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botmetadata;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.text.MessageFormat.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.byteSources.ByteSource;
import org.apache.sanselan.common.byteSources.ByteSourceFile;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.jpeg.JpegPhotoshopMetadata;
import org.apache.sanselan.formats.jpeg.iptc.IPTCConstants;
import org.apache.sanselan.formats.jpeg.iptc.IPTCRecord;
import org.apache.sanselan.formats.jpeg.iptc.JpegIptcRewriter;
import org.apache.sanselan.formats.jpeg.iptc.PhotoshopApp13Data;

/**
 *
 * @author ciberado
 */
/* ---------------------------------------------------------------------
// reading meta, just an example
for (File file : fileList) {
log.info("Processing " + file);
IImageMetadata meta =  Sanselan.getMetadata(file);
JpegImageMetadata jmeta = (JpegImageMetadata) meta;
for (Object item : jmeta.getPhotoshop().getItems()) {
System.out.println(item + " <<"+item.getClass().getName()+">>");
}
}
----------------------------------------------------------------------*/
public class IPTCKeywordsDataUpdater implements DataUpdater {

    private static final Log log = LogFactory.getLog(IPTCKeywordsDataUpdater.class);

    public IPTCKeywordsDataUpdater() {
    }

    @Override
    public void updateInfo(File originalFile, String speciesName, String authorName, String regionType)
            throws RuntimeException /*ImageReadException | ImageWriteException | IOException*/ {
        File newFile = null;
        BufferedOutputStream destinationStream = null;
        try {
            String[] keywordValuesArray = {"sp:" + speciesName,
                                           "author:" + authorName,
                                           "regionType:" + regionType};

            log.info("Processing " + originalFile);
            ByteSource byteSource = new ByteSourceFile(originalFile);
            Map params = new HashMap();
            params.put(Sanselan.PARAM_KEY_READ_THUMBNAILS, false);
            IImageMetadata meta = Sanselan.getMetadata(originalFile);

            JpegPhotoshopMetadata metadata =
                    new JpegImageParser().getPhotoshopMetadata(byteSource, params);
            List newRecords = new ArrayList();
            List<String> keywordValues = Arrays.asList(keywordValuesArray);
            if (metadata != null) {
                for (Object record : metadata.getItems()) {
                    if (record instanceof IPTCRecord) {
                        IPTCRecord oldRecord = (IPTCRecord) record;
                        if (IPTCConstants.IPTC_TYPE_KEYWORDS.name.equals(oldRecord.getIptcTypeName()) == true) {
                            String foundKeyword = findKeyword(oldRecord.getValue(), keywordValuesArray);
                            if ((foundKeyword != null) && (foundKeyword.equals(oldRecord.getValue()) == false)) {
                                // the new value will be added in a later fase
                                log.debug(format("Keyword value {0} will be updated to {1}.",
                                        oldRecord.getValue(), foundKeyword));
                            } else {
                                log.debug(format("Keeping keyword value {0}.", oldRecord.getValue()));
                                if (foundKeyword != null) {
                                    keywordValues.remove(foundKeyword);
                                }
                                newRecords.add(oldRecord);
                            }
                        }
                    } else {
                        log.warn(MessageFormat.format("Record not processed: {0}.", record));
                    }
                }
            }
            if (keywordValues.size() > 0) {
                for (String keywordValue : keywordValues) {
                    log.debug(format("Adding additional keyword {0}.", keywordValue));
                    newRecords.add(new IPTCRecord(IPTCConstants.IPTC_TYPE_KEYWORDS, keywordValue));
                }
                log.info(format("Rewriting file {0}.", originalFile.getName()));
                newFile = new File(originalFile.getAbsolutePath() + ".new");
                destinationStream =
                        new BufferedOutputStream(new FileOutputStream(newFile));
                PhotoshopApp13Data newData = new PhotoshopApp13Data(
                        newRecords, (metadata == null) ? new ArrayList() : metadata.photoshopApp13Data.getRawBlocks());
                new JpegIptcRewriter().writeIPTC(byteSource, destinationStream, newData);
                destinationStream.close();
                originalFile.renameTo(new File(originalFile.getAbsolutePath() + ".backup"));
                newFile.renameTo(originalFile);
            } else {
                log.debug("IPTC records already present, skipping file.");
            }
        } catch (ImageReadException | ImageWriteException | IOException exc) {
            IOUtils.closeQuietly(destinationStream);
            if (newFile != null) {
                newFile.delete();
            }
            log.warn(format("Error updating file {0} [{1}].", originalFile.getAbsolutePath(), exc));
            throw new RuntimeException(exc);
        }

    }


    private String findKeyword(String oldKeywordValue, String[] newKeywordValues) {
        String foundKeyword = null;
        if (oldKeywordValue.indexOf(":") != -1) {
            String prefix = oldKeywordValue.substring(0, oldKeywordValue.indexOf(":"));
            String value = oldKeywordValue.substring(prefix.length() + 1);
            for (String currentKeyword : newKeywordValues) {
                String currentPrefix = currentKeyword.substring(0, currentKeyword.indexOf(":"));
                String currentValue = currentKeyword.substring(currentPrefix.length() + 1);
                if ((prefix.equals(currentPrefix) == true)
                        && (value.equals(currentValue) == false)) {
                    foundKeyword = currentKeyword;
                    break;
                }
            }
        }

        return foundKeyword;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end(boolean ok) {
    }
}
