/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.datapublishing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ciberado
 */
public class InfoCard {
    
    private Map<String /*attr name*/, Object /*attr value*/> attributes = new HashMap<String, Object>();

    public InfoCard() {
    }
    
    public void set(String attrName, Object attrValue) {
        attributes.put(attrName, attrValue);
    }
   
    public Object get(String attrName) {
        return attributes.get(attrName);
    }
    
    public Set<String> getAttributesNameSet() {
        return attributes.keySet();
    }
    
    public int getSize() {
        return attributes.size();
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
    
    
}
