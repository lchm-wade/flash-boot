package com.foco.boot.db.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description: 字段自动填充配置
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Data
@ConfigurationProperties(prefix = FieldAutoFillProperties.PREFIX)
public class FieldAutoFillProperties {

    public static final String PREFIX="mybatis-plus.global-config.db-config.auto-fill-field";
    private boolean enabled=true;
    /**创建人ID*/
    private String createId="createId";
    /**创建人名称*/
    private String createName="createName";
    /**创建时间*/
    private String createTime="createTime";

    /**更新人ID*/
    private String modifyId="modifyId";
    /**更新人名称*/
    private String modifyName="modifyName";
    /**更新时间*/
    private String modifyTime="modifyTime";
    /**删除标记*/
    private String deleted="deleted";

    private String tenantId="tenantId";
    /**
     * 时区 默认东八区
     */
    private String timeZone="+8";
}
