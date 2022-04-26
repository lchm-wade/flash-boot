package com.foco.boot.dynamic.source;

import java.lang.annotation.*;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/4 14:34
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    String value();
}
