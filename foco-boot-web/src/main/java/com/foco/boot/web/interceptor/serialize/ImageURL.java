package com.foco.boot.web.interceptor.serialize;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 *对图片字段加域名前缀和指定后缀
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = ImageURLSerialize.class)
public @interface ImageURL {
    String domainPrefix() default ""; //支持${spring.application.name}从Environment中取值
   String suffix() default "";//支持${spring.application.name}从Environment中取值
    String separator() default ",";//支持多个字符串逗号分隔
}
