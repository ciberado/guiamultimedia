/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.tts;

/**
 *
 * @author ciberado
 */
public interface DescriptionDataSource {

    public String getDescription(String language, String query);

}
