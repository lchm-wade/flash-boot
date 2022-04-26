package com.foco.boot.web.autoconfigure;

import com.foco.boot.web.properties.ParamLogProperties;
import com.foco.boot.web.interceptor.ParamLogAspect;
import com.foco.model.constant.FocoConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/09/29 16:03
 */
@EnableConfigurationProperties(ParamLogProperties.class)
public class ParamLogAutoConfiguration {
    @Bean
    @ConditionalOnProperty(prefix =  ParamLogProperties.PREFIX,name = FocoConstants.ENABLED,matchIfMissing = true)
    public ParamLogAspect paramLogAspect(){
        return new ParamLogAspect();
    }
}
