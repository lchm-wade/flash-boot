package com.foco.boot.web.support;

import cn.hutool.core.collection.CollectionUtil;
import com.foco.model.constant.FocoConstants;
import com.foco.model.constant.MainClassConstant;
import com.foco.model.spi.ExcludeAutoConfigure;
import com.foco.model.spi.ExcludeAutoConfigureManager;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @Description
 * @Author lucoo
 * @Date 2021/6/13 15:07
 **/
@Slf4j
public class ExcludeAutoConfigurationEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        handlerCustom(environment);
    }
    private void handlerCustom(ConfigurableEnvironment environment) {
        Properties focoProperties = new Properties();
        focoProperties.put("spring.mvc.throwExceptionIfNoHandlerFound", true);
       // focoProperties.put("spring.resources.addMappings", false);
        focoProperties.put("spring.servlet.multipart.maxFileSize", "1024MB");
        focoProperties.put("spring.servlet.multipart.maxRequestSize", "1024MB");
        List<String> excludeClassList=new ArrayList();
        List<ExcludeAutoConfigure> excludeAutoConfigures = ExcludeAutoConfigureManager.getExcludeAutoConfigures();
        if(CollectionUtil.isNotEmpty(excludeAutoConfigures)){
            for(ExcludeAutoConfigure excludeAutoConfigure:excludeAutoConfigures){
                excludeAutoConfigure.exclude(excludeClassList,environment);
            }
        }
        ArrayList<String> excludeFinalClass = Lists.newArrayList(MainClassConstant.DATA_SOURCE_AUTO_CONFIGURATION_CLASS);
        excludeFinalClass.addAll(excludeClassList);
        focoProperties.put("spring.autoconfigure.exclude", StringUtils.join(excludeFinalClass));
        environment.getPropertySources().addLast(new PropertiesPropertySource("foco-exclude-autoconfig", focoProperties));
    }
}
