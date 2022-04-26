package com.foco.boot.web.autoconfigure;

import cn.hutool.core.collection.CollectionUtil;
import com.foco.boot.web.interceptor.*;
import com.foco.boot.web.interceptor.resolver.CurrentUserMethodArgumentResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

/**
 * description: web配置
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
@Slf4j
@Configuration
public class CustomWebMvcConfigurer implements WebMvcConfigurer{
    @Autowired(required = false)
    private List<CustomInterceptorRegistry> customInterceptorRegistrys;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (CollectionUtil.isNotEmpty(customInterceptorRegistrys)) {
            for(CustomInterceptorRegistry customInterceptorRegistry:customInterceptorRegistrys){
                customInterceptorRegistry.addInterceptor(registry);
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html","doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserMethodArgumentResolver());
    }
}