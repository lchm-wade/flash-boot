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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description 时间戳转LocalDate
 * @date 2021-07-22 17:36
 */
public class JacksonLocalDateDeserializer extends JsonDeserializer<LocalDate> implements ContextualDeserializer {
    private TimeStamp dateSerialize;

    public JacksonLocalDateDeserializer(TimeStamp dateSerialize) {
        this.dateSerialize = dateSerialize;
    }

    public JacksonLocalDateDeserializer() {
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        long value = jsonParser.getValueAsLong();
        if(dateSerialize.equals(TimeStamp.TIME_STAMP_MS)){
            return Instant.ofEpochMilli(value).atZone(ZoneOffset.ofHours(8)).toLocalDate();
        }else {
            return Instant.ofEpochSecond(value).atZone(ZoneOffset.ofHours(8)).toLocalDate();
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if(beanProperty!=null){
            if (Objects.equals(beanProperty.getType().getRawClass(), LocalDate.class)) {
                LocalDateDeserialize localDateTimeDeserialize = beanProperty.getAnnotation(LocalDateDeserialize.class);
                if (localDateTimeDeserialize != null) {
                    return new JacksonLocalDateDeserializer(localDateTimeDeserialize.dateSerialize());
                }
            }
        }

        return deserializationContext.findContextualValueDeserializer(beanProperty.getType(), beanProperty);
    }
}