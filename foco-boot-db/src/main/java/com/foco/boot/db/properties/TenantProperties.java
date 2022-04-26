package com.foco.boot.db.properties;

import com.foco.properties.AbstractProperties;
import com.foco.model.constant.FocoConstants;
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
@ConfigurationProperties(prefix = TenantProperties.PREFIX)
@Getter
@Setter
public class TenantProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"tenant";
    public static TenantProperties getConfig(){
        return getConfig(TenantProperties.class);
    }
    /**
     * 是否开启多租户,默认关闭
     */
    private boolean enabled=false;
    /**
     * 忽略多租户,表名
     */
    private List<String> ignoreTables=new ArrayList<>();
    /**
     * 数据库中租户id的列名
     */
    private String tenantIdColumnName="tenant_id";
}
