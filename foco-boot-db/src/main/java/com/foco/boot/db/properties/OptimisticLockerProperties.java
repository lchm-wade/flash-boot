package com.foco.boot.db.properties;

import com.foco.model.constant.FocoConstants;
import com.foco.properties.AbstractProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/2 14:43
 **/
@ConfigurationProperties(prefix = OptimisticLockerProperties.PREFIX)
@Getter
@Setter
public class OptimisticLockerProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"optimistic";
    public static OptimisticLockerProperties getConfig(){
        return getConfig(OptimisticLockerProperties.class);
    }
    /**
     * 是否乐观锁插件,默认关闭
     */
    private boolean enabled=false;
}
