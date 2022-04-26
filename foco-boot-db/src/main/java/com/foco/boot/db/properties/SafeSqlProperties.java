package com.foco.boot.db.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;


/**
 * <p> 安全sql配置类 </p>
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Data
@ConfigurationProperties(prefix = SafeSqlProperties.PREFIX)
public class SafeSqlProperties {

    public final static String PREFIX = "mybatis-plus.global-config.db-config.sql-safe";
    /**
     * 启用安全SQL拦截
     */
    private boolean enabled=false;
    /**
     * 启用安全删除
     */
    private boolean enableSafeDelete = true;
    /**
     * 启用安全更新
     */
    private boolean enableSafeUpdate = true;
    /**
     * 安全删除校验忽略的表
     */
    private Set<String> safeDeleteIgnoreTable=new HashSet<>();
    /**
     * 安全更新校验忽略的表
     */
    private Set<String> safeUpdateIgnoreTable=new HashSet<>();
}
