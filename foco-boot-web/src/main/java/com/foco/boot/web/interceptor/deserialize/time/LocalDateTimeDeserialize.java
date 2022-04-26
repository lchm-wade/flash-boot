package com.foco.boot.web.interceptor.deserialize.time;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.foco.boot.web.interceptor.serialize.time.TimeStamp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = JacksonLocalDateTimeDeserializer.class)
public @interface LocalDateTimeDeserialize {
    TimeStamp dateSerialize() default TimeStamp.TIME_STAMP;
}
