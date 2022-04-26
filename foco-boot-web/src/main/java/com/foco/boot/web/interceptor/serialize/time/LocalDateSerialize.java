package com.foco.boot.web.interceptor.serialize.time;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = JacksonLocalDateSerializer.class)
public @interface LocalDateSerialize {
    TimeStamp dateSerialize() default TimeStamp.TIME_STAMP;
}
