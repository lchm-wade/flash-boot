package com.foco.boot.web.interceptor.serialize;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO
 *  敏感字段处理
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveHandlerSerialize.class)
public @interface SensitiveHandler {
    int start() default 0;
    int end() default 3000;
    String replaceStr() default "*";
    //占位符的长度,如没有给定值，取字符串的实际长度(适用于密码字段的处理,防止根据密码长度猜测密码)
    //给定了该值，则start 和end将失效,会全替换
    int replaceLen() default 0;
}
