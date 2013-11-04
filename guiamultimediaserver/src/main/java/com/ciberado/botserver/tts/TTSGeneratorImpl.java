/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

/**
 *
 * @author ciberado
 */
public abstract class TTSGeneratorImpl implements TTSGenerator {
    
    protected String generateFilame(String text) {
        StringBuilder name = new StringBuilder();
        for (int idx = 0; idx < text.length() && idx < 15; idx++) {
            if (Character.isLetter(text.charAt(idx)) == true) {
                name.append(text.charAt(idx));
            }
        }
        return System.getProperty("java.io.tmpdir") + "/" + name + "_" + System.currentTimeMillis() + ".mp3";
    }

    

}
