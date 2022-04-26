package com.foco.boot.web.interceptor.deserialize.time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.foco.boot.web.interceptor.serialize.time.TimeStamp;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description 时间戳转Date
 * @date 2021-07-22 17:36
 */
public class JacksonDateDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {
    private TimeStamp dateSerialize;

    public JacksonDateDeserializer(TimeStamp dateSerialize) {
        this.dateSerialize = dateSerialize;
    }

    public JacksonDateDeserializer() {
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        long value = jsonParser.getValueAsLong();
        if(dateSerialize.equals(TimeStamp.TIME_STAMP_MS)){
            return new Date(value);
        }else {
            return new Date(value*1000);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if(beanProperty!=null){
            if (Objects.equals(beanProperty.getType().getRawClass(), Date.class)) {
                DateDeserialize dateTimeDeserialize = beanProperty.getAnnotation(DateDeserialize.class);
                if (dateTimeDeserialize != null) {
                    return new JacksonDateDeserializer(dateTimeDeserialize.dateSerialize());
                }
            }
        }
        return deserializationContext.findContextualValueDeserializer(beanProperty.getType(), beanProperty);
    }
}