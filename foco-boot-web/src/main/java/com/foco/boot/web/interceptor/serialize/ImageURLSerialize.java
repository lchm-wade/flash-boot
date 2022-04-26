package com.foco.boot.web.interceptor.serialize;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.foco.context.core.SpringContextHolder;
import com.foco.context.util.HttpContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * TODO
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
public class ImageURLSerialize extends JsonSerializer<String> implements ContextualSerializer {
    private String domainPrefix;
    private String suffix;
    private String separator;
    private Environment environment;

    public ImageURLSerialize() {
    }

    public ImageURLSerialize(String domainPrefix, String suffix,String separator, Environment environment) {
        this.domainPrefix = domainPrefix;
        this.suffix = suffix;
        this.environment = environment;
        this.separator=separator;
    }

    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        StringBuilder builder = new StringBuilder();
        String finalVal = value;
        if (HttpContext.isNotFeignRequest()&&StrUtil.isNotEmpty(value)) {
            String[] values = value.split(separator);
            for (String singleValue : values) {
                String mergedValue = "";
                if (!StringUtils.isEmpty(domainPrefix)) {
                    mergedValue = handlerDynamicName(domainPrefix) + singleValue;
                }
                if (!StringUtils.isEmpty(suffix)) {
                    mergedValue = mergedValue + handlerDynamicName(suffix);
                }
                if (builder.length() != 0) {
                    builder.append(",");
                }
                builder.append(mergedValue);
            }
            finalVal = builder.toString();
        }
        jsonGenerator.writeString(finalVal);
    }

    private String handlerDynamicName(String param) {
        String resolvedParam = param;
        if (param.startsWith("${")) {
            Environment environment = SpringContextHolder.getBean(Environment.class);
            resolvedParam = environment.resolvePlaceholders(param);
        }
        return resolvedParam;
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
                ImageURL imageURL=beanProperty.getAnnotation(ImageURL.class);
                if (imageURL != null) {
                    Environment environment = SpringContextHolder.getBean(Environment.class);
                    return new ImageURLSerialize(imageURL.domainPrefix(), imageURL.suffix(),imageURL.separator(), environment);
                }
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.getDefaultNullValueSerializer();
    }
}
