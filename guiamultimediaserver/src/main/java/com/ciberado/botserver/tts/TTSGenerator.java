/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

import java.io.File;

/**
 *
 * @author ciberado
 */
public interface TTSGenerator {

    File generateVoiceFile(String language, String sentence);

}
