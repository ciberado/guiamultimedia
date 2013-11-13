/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.datapublishing;

import com.ciberado.botserver.model.WikipediaPageResume;
import com.ciberado.lang.SystemException;
import java.io.IOException;
import java.net.URL;
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
    public WikipediaPageResume getResume(String wikipediaLink) {
        try {
            WikipediaPageResume page = new WikipediaPageResume();
            page.setUrl(wikipediaLink);
            Document doc = Jsoup.connect(wikipediaLink).get();
            Elements textParagraphs = doc.select("#mw-content-text > p");
            StringBuilder textResume = new StringBuilder();
            for (Element elem : textParagraphs) {
                String text = elem.text();
                if (textResume.length() + text.length() < 600) {
                    textResume.append(text);
                } else {
                    break;
                }
            } 
            page.setText(textResume.toString().replaceAll("\\[.*\\]", ""));
            Elements galleryImages = doc.select("#mw-content-text a.image");
            for (Element imgElem : galleryImages) {
                URL url = new URL(wikipediaLink);                
                String imageUrl = "//" + url.getHost() + imgElem.attr("href");
                if (isValidImageResource(imageUrl) == true) {
                    String thumbnail = imgElem.getElementsByTag("img").attr("src");
                    page.addImage(imageUrl, thumbnail);
                }
            }
            
            return page;
        } catch (IOException exc) {
            throw new SystemException(exc);
        }
    }
    
    private String[] invalidResources = {
      "Wikispecies-logo", "Commons-logo", "Nuvola_apps_kuickshow"  
    };
    private boolean isValidImageResource(String url) {
        boolean result = true;
        for (String resource : invalidResources) {
            if (url.contains(resource) == true) { 
                result = false;
                break;
            }
        }
        return result;
    }
}
