/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.lang.SystemException;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author ciberado
 */
public class WikipediaServiceImpl implements WikipediaService {

    @Cacheable("wikipediaResumes")
    public String getResume(String wikipediaLink) {
        try {
            Document doc = Jsoup.connect(wikipediaLink).get();
            Elements textParagraphs = doc.select("#mw-content-text > p");
            StringBuilder resume = new StringBuilder();
            for (Element elem : textParagraphs) {
                String text = elem.text();
                if (resume.length() + text.length() < 600) {
                    resume.append(text);
                } else {
                    break;
                }
            } 
            return resume.toString().replaceAll("\\[.*\\]", "");                    
        } catch (IOException exc) {
            throw new SystemException(exc);
        }
    }
    
}
