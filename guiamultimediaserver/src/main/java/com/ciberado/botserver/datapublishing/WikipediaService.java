/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.WikipediaPageResume;

/**
 *
 * @author ciberado
 */
public interface WikipediaService {

    WikipediaPageResume getResume(String wikipediaLink);
    
}
