package com.foco.boot.web.support.pulish;

/**
 * TODO
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
public interface EventListener<T> {
    void onChange(T t);
}
