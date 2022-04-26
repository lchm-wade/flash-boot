package com.foco.boot.dynamic.source.autoconfigure;

import cn.hutool.core.util.StrUtil;
import com.foco.boot.db.bootstrap.DbBootstrapConfiguration;
import com.foco.boot.dynamic.source.DataSourceUtil;
import com.foco.boot.dynamic.source.DynamicDataSource;
import com.foco.boot.dynamic.source.DynamicDataSourceTransmit;
import com.foco.boot.dynamic.source.DynamicDattaSourceAspect;
import com.foco.boot.dynamic.source.properties.DynamicProperties;
import com.foco.context.util.BootStrapPrinter;
import com.foco.context.core.DataSourceContextHolder;
import com.foco.context.util.PropertyUtil;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.constant.MainClassConstant;
import com.foco.model.exception.SystemException;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/4 15:45
 **/
@ConditionalOnMissingClass(MainClassConstant.SHARDING_JDBC)
@EnableConfigurationProperties(DynamicProperties.class)
@AutoConfigureBefore(name = MainClassConstant.MYBATIS_PLUS)
@Slf4j
public class DynamicDataSourceAutoConfiguration {
    @PostConstruct
    public void init(){
        BootStrapPrinter.log("foco-boot-dynamic-source",this.getClass());
    }
    @Bean
    DynamicDattaSourceAspect dynamicDattaSourceAspect(){
        return new DynamicDattaSourceAspect();
    }
    @Bean
    @Primary
    DynamicDataSource dynamicDataSource(Environment environment){
        DynamicDataSource dynamicDataSource=new DynamicDataSource();
        Map<Object, Object> dataSourcesMap=new HashMap<>();
        String prefix = "spring.datasource.";
        String dataSources = environment.getProperty(prefix + "names");
        if(StrUtil.isBlank(dataSources)){
            SystemException.throwException(FocoErrorCode.CONFIG_VALID.getCode(),"spring.datasource.names must not null");
        }
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + each.trim(), Map.class);
                log.info("数据源信息:{}",dataSourceProps);
                Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
                DataSource dataSource = DataSourceUtil
                        .getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                if(DataSourceContextHolder.masterDsName.equals(each.trim())){
                    dynamicDataSource.setDefaultTargetDataSource(dataSource);
                }else {
                    dataSourcesMap.put(each, dataSource);
                }
                DataSourceContextHolder.addDataSourceId(each.trim());
            } catch (final ReflectiveOperationException ex) {
                throw new SystemException("Can't find datasource type!", ex);
            }
        }
        dynamicDataSource.setTargetDataSources(dataSourcesMap);
        return dynamicDataSource;
    }
    @Bean
    DynamicDataSourceTransmit dynamicDataSourceTransmit(){
        return new DynamicDataSourceTransmit();
    }
}
