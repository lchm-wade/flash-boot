package com.foco.boot.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @Author lucoo
 * @Date 2021/6/6 18:28
 */
@Data
public class BaseTenantExtendEntity extends BaseExtendEntity {
    /**
     * 创建人名称
     */
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private Long tenantId;
}
