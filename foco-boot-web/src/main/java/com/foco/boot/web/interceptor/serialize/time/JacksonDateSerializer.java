package com.foco.boot.web.interceptor.serialize.time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.foco.context.util.HttpContext;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description 时间类型返回时间戳
 * @date 2021-07-22 17:36
 */
public class JacksonDateSerializer extends JsonSerializer<Date> implements ContextualSerializer {
    private TimeStamp dateSerialize;

    public JacksonDateSerializer(TimeStamp dateSerialize) {
        this.dateSerialize = dateSerialize;
    }

    public JacksonDateSerializer() {
    }

    @Override
    public void serialize(Date dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(HttpContext.isNotFeignRequest()){
            if(dateSerialize.equals(TimeStamp.TIME_STAMP_MS)){
                jsonGenerator.writeString(String.valueOf(dateTime.getTime()));
            }else {
                jsonGenerator.writeString(String.valueOf((dateTime.getTime())/1000));
            }
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), Date.class)) {
                DateSerialize localDateTimeSerialize = beanProperty.getAnnotation(DateSerialize.class);
                if (localDateTimeSerialize != null) {
                    return new JacksonDateSerializer(localDateTimeSerialize.dateSerialize());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.getDefaultNullValueSerializer();
    }
}
