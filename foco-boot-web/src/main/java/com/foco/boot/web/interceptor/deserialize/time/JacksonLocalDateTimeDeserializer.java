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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description 时间戳转LocalDateTime
 * @date 2021-07-22 17:36
 */
public class JacksonLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> implements ContextualDeserializer {
    private TimeStamp dateSerialize;

    public JacksonLocalDateTimeDeserializer(TimeStamp dateSerialize) {
        this.dateSerialize = dateSerialize;
    }

    public JacksonLocalDateTimeDeserializer() {
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        long value = jsonParser.getValueAsLong();
        if (dateSerialize.equals(TimeStamp.TIME_STAMP_MS)) {
            return LocalDateTime.ofEpochSecond(value / 1000, 0, ZoneOffset.ofHours(8));
        } else {
            return LocalDateTime.ofEpochSecond(value, 0, ZoneOffset.ofHours(8));
        }
    }
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), LocalDateTime.class)) {
                LocalDateTimeDeserialize localDateTimeDeserialize = beanProperty.getAnnotation(LocalDateTimeDeserialize.class);
                if (localDateTimeDeserialize != null) {
                    return new JacksonLocalDateTimeDeserializer(localDateTimeDeserialize.dateSerialize());
                }
            }
        }
        return deserializationContext.findContextualValueDeserializer(beanProperty.getType(), beanProperty);
    }
}
