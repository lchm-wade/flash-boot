package com.foco.boot.web.interceptor.serialize;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.foco.context.util.HttpContext;

import java.io.IOException;
import java.util.Objects;

/**
 * TODO
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
public class SensitiveHandlerSerialize extends JsonSerializer<String> implements ContextualSerializer {
    private int start;
    private int end;
    private String replaceStr;
    private int replaceLen;

    public SensitiveHandlerSerialize() {
    }

    public SensitiveHandlerSerialize(int start, int end, String replaceStr, int replaceLen) {
        this.start = start;
        this.end = end;
        this.replaceStr = replaceStr;
        this.replaceLen = replaceLen;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(fillVal(value));
    }

    private String fillVal(String value) {
        if (HttpContext.isNotFeignRequest() && StrUtil.isNotEmpty(value)) {
            StringBuffer buffer = new StringBuffer();
            int replaceStrLen;
            if (replaceLen != 0) {
                //根据给定的长度,将字段全替换
                for (int i = 0; i < replaceLen; i++) {
                    buffer.append(replaceStr);
                }
                return buffer.toString();
            } else {
                if (start > end || start > value.length()) {
                    return value;
                }
                if (start <= 0) {
                    start = 1;
                }
                if (end > value.length()) {
                    end = value.length();
                }
                replaceStrLen = (end - start) + 1;
                for (int i = 0; i < replaceStrLen; i++) {
                    buffer.append(replaceStr);
                }
                return value.substring(0, start - 1) + buffer.toString() + value.substring(end);
            }
        }
        return value;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                SensitiveHandler sensitiveHandler = beanProperty.getAnnotation(SensitiveHandler.class);
                if (sensitiveHandler != null) {
                    return new SensitiveHandlerSerialize(sensitiveHandler.start(), sensitiveHandler.end(),
                            sensitiveHandler.replaceStr(), sensitiveHandler.replaceLen());
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.getDefaultNullValueSerializer();
    }
}
