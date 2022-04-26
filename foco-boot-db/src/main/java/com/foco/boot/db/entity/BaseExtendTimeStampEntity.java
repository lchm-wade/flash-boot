package com.foco.boot.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @Author lucoo
 * @Date 2021/6/6 18:28
 */
@Data
public class BaseExtendTimeStampEntity extends BaseTimeStampEntity {
    /**
     * 创建人名称
     */
    @TableField(value = "create_name", fill = FieldFill.INSERT)
    private String createName;
    /**
     * 修改人名称
     */
    @TableField(value = "modify_name", fill = FieldFill.INSERT_UPDATE)
    private String modifyName;
    /**
     * 是否删除 1是 0不是
     */
    @TableField(value = "deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
