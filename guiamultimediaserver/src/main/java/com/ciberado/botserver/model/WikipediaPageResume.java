/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ciberado.botserver.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ciberado
 */
public class WikipediaPageResume implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String url;
    
    private String text;
    
    private Set<Image> images = new HashSet<Image>();

    public WikipediaPageResume() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public Set<Image> getImages() {
        return images;
    }
    
    public void addImage(String originalUrl, String thumbnailUrl) {
        images.add(new Image(originalUrl, thumbnailUrl));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.url != null ? this.url.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WikipediaPageResume other = (WikipediaPageResume) obj;
        if ((this.url == null) ? (other.url != null) : !this.url.equals(other.url)) {
            return false;
        }
        return true;
    }
    
    public static class Image implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String imageUrl;
        private String thumbnailUrl;

        public Image(String imageUrl, String thumbnailUrl) {
            this.imageUrl = imageUrl;
            this.thumbnailUrl = thumbnailUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String originalUrl) {
            this.imageUrl = originalUrl;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public void setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (this.imageUrl != null ? this.imageUrl.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Image other = (Image) obj;
            if ((this.imageUrl == null) ? (other.imageUrl != null) : !this.imageUrl.equals(other.imageUrl)) {
                return false;
            }
            return true;
        }
        
        
    }
    
    
}
