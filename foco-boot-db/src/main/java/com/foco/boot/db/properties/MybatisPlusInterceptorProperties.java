package com.foco.boot.db.properties;

import com.foco.model.constant.FocoConstants;
import com.foco.properties.AbstractProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/10/12 17:39
 */
@ConfigurationProperties(prefix = MybatisPlusInterceptorProperties.PREFIX)
@Getter
@Setter
public class MybatisPlusInterceptorProperties extends AbstractProperties {
    private boolean enabled=true;
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"mp.interceptor";
    public static MybatisPlusInterceptorProperties getConfig(){
        return getConfig(MybatisPlusInterceptorProperties.class);
    }
}
