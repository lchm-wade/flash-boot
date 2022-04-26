package com.foco.boot.web.interceptor;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * @Description 业务层扩展的拦截器注册器
 * @Author lucoo
 * @Date 2021/6/24 18:14
 **/
public interface CustomInterceptorRegistry {
    void addInterceptor(InterceptorRegistry registry);
}
