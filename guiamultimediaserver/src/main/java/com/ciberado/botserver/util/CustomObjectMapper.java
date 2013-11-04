/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.util;

import com.ciberado.botserver.datapublishing.InfoCard;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.stereotype.Component;

/**
 *
 * @author ciberado
 */
@Component
public class CustomObjectMapper extends ObjectMapper {

    
    public CustomObjectMapper() {
        super();
        
        SimpleModule module =  new SimpleModule("CustomSerializerModule",  
                                                new Version(1, 0, 0, null));
        module.addSerializer(Geometry.class, new JsonGeometrySerializer());
        module.addSerializer(InfoCard.class, new InfoCardSerializer());
        super.registerModule(module);           
//        super.getSerializationConfig().set( 
//                SerializationConfig.Feature.INDENT_OUTPUT, true);
        super.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
    }
    
    
    
    class JsonGeometrySerializer extends JsonSerializer<Geometry> {

        @Override
        public void serialize(Geometry geo, JsonGenerator gen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
            gen.writeString(geo.toString());
        }
    }
    
    
    class InfoCardSerializer extends JsonSerializer<InfoCard> {
        @Override
        public void serialize(InfoCard infoCard, JsonGenerator gen, SerializerProvider provider)
        throws IOException, JsonProcessingException {            
            gen.writeStartObject();
            for (String currentAttrName : infoCard.getAttributesNameSet()) {
                Object value = infoCard.get(currentAttrName);
                if (value == null) {
                    gen.writeNullField(currentAttrName);
                } else if (value instanceof Number) {
                    Number number = (Number) value;
                    if ((value instanceof Integer) || (value instanceof Long)) {
                        gen.writeNumberField(currentAttrName, number.longValue());
                    } else {
                        gen.writeNumberField(currentAttrName, number.doubleValue());
                    }
                } else {
                    gen.writeStringField(currentAttrName, String.valueOf(value));
                }
            }
            gen.writeEndObject();
        }
    }
    
    
}

