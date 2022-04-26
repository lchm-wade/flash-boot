package com.foco.boot.db.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Data
@ConfigurationProperties(prefix = SqlLogProperties.PREFIX)
public class SqlLogProperties {

    public final static String PREFIX = "mybatis-plus.global-config.db-config.sql-log";
    /**
     * 是否开启sql日志打印
     */
    private boolean enabled=false;
    /**
     * 是否打印sql查询结果
     */
    private boolean printRsp=false;
    /**
     * 是否打印sql请求消耗时间
     */
    private boolean printTime=false;
}
