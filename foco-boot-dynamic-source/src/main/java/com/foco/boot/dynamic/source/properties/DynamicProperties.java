package com.foco.boot.dynamic.source.properties;

import com.foco.model.constant.FocoConstants;
import com.foco.properties.AbstractProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/10/14 15:33
 */
@ConfigurationProperties(prefix = DynamicProperties.PREFIX)
@Getter
@Setter
public class DynamicProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"dynamic";
    public static DynamicProperties getConfig(){
        return getConfig(DynamicProperties.class);
    }
    /**
     * 是否开启读写分离 默认不开启
     */
    private Boolean readWriteSplitting=false;
    /**
     * 走从库读的方法
     */
    private List<String> readMethod;
}
