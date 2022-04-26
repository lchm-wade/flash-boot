package com.foco.boot.mongo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 标识主键ID需要自动增长
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface MongoAutoInc {

}
