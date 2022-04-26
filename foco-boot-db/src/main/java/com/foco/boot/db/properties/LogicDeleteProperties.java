package com.foco.boot.db.properties;

import com.foco.properties.AbstractProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Data
@ConfigurationProperties(prefix = LogicDeleteProperties.PREFIX)
public class LogicDeleteProperties extends AbstractProperties {
    public static final String PREFIX="mybatis-plus.global-config.db-config.logic-delete";

    private boolean enabled=false;
    /**逻辑删除字段*/
    private String columnName="deleted";
    /**删除标记值*/
    private String deleteValue="1";
    /**未删除标记值*/
    private String noDeleteValue="0";

    /**忽略的表*/
    private Set<String> ignoreTableName=new HashSet<>();
}
