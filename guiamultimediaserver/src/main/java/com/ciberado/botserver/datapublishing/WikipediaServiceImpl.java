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
                text = text.replaceAll("\\[.*?\\]", " ");
                textResume.append(text).append(" ");
                if (textResume.length() > 600) {
                    break;
                }
            } 
            while (textResume.length() > 600) {
                do {
                    textResume.deleteCharAt(textResume.length()-1);            
                } while (textResume.length() > 0 && textResume.charAt(textResume.length()-1) != '.');
            }
            page.setText(textResume.toString());
            Elements galleryImages = doc.select("#mw-content-text a.image");
            for (Element linkElem : galleryImages) {
                URL url = new URL(wikipediaLink);                
                String imageUrl = "//" + url.getHost() + linkElem.attr("href");
                if (isValidImageResource(imageUrl) == false) {
                    continue;
                }
                Element imgElem = linkElem.getElementsByTag("img").first();
                String width = imgElem.attr("width");
                if ((width != null) && (width.isEmpty() == false) && (Integer.parseInt(width) <= 50)) {
                    continue;
                }
                String thumbnail = imgElem.attr("src");
                page.addImage(imageUrl, thumbnail);
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
