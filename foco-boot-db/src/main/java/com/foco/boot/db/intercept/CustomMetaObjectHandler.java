package com.foco.boot.db.intercept;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.foco.boot.db.properties.FieldAutoFillProperties;
import com.foco.boot.db.properties.LogicDeleteProperties;
import com.foco.boot.db.properties.TenantProperties;
import com.foco.context.core.LoginContext;
import com.foco.context.core.LoginContextHolder;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

/**
 * description: 自动填充字段值
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Slf4j
public class CustomMetaObjectHandler implements MetaObjectHandler {
    @Autowired
    private FieldAutoFillProperties fieldAutoFillProperties;
    @Autowired
    private LogicDeleteProperties logicDeleteProperties;
    @Autowired
    private TenantProperties tenantProperties;

    @Override
    public void insertFill(MetaObject metaObject) {
        fillCreateTime(metaObject);
        fillCreateId(metaObject);
        fillTenantId(metaObject);
        fillCreateName(metaObject);
        fillDeleted(metaObject);
        updateFill(metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        fillModifyTime(metaObject);
        fillModifyId(metaObject);
        fillModifyName(metaObject);
    }

    private void fillDeleted(MetaObject metaObject) {
        try {
            if (logicDeleteProperties.isEnabled() && metaObject.hasGetter(fieldAutoFillProperties.getDeleted())) {
                Object fieldValue = metaObject.getValue(fieldAutoFillProperties.getDeleted());
                if (fieldValue == null) {
                    TableInfo tableInfo = TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());
                    boolean match = logicDeleteProperties.getIgnoreTableName().stream().anyMatch(
                            (t) -> t.equalsIgnoreCase(tableInfo.getTableName())
                    );
                    if (match) {
                        return;
                    }
                    Class<?> clazz = metaObject.getSetterType(fieldAutoFillProperties.getDeleted());
                    if (clazz.isAssignableFrom(Integer.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getDeleted(), Integer.class, Integer.valueOf(logicDeleteProperties.getNoDeleteValue()));
                    } else if (clazz.isAssignableFrom(Boolean.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getDeleted(), Boolean.class, Boolean.valueOf(logicDeleteProperties.getNoDeleteValue()));
                    } else {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getDeleted(), Byte.class, Byte.valueOf(logicDeleteProperties.getNoDeleteValue()));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getDeleted());
        }
    }

    private void fillCreateTime(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getCreateTime())) {
                Class<?> setterType = metaObject.getSetterType(fieldAutoFillProperties.getCreateTime());
                if (setterType.isAssignableFrom(LocalDateTime.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateTime(), LocalDateTime.class, LocalDateTime.now());
                } else if (setterType.isAssignableFrom(Date.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateTime(), Date.class, new Date());
                } else if (setterType.isAssignableFrom(LocalDate.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateTime(), LocalDate.class, LocalDate.now());
                } else if (setterType.isAssignableFrom(Long.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateTime(), Long.class, LocalDateTime.now().toInstant(ZoneOffset.of(fieldAutoFillProperties.getTimeZone())).toEpochMilli());
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getCreateTime());
        }
    }

    private void fillCreateId(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getCreateId())) {
                Object fieldValue = metaObject.getValue(fieldAutoFillProperties.getCreateId());
                if (fieldValue == null) {
                    Class<?> clazz = metaObject.getSetterType(fieldAutoFillProperties.getCreateId());
                    LoginContext loginUser = LoginContextHolder.getLoginContext(LoginContext.class);
                    String userId = Optional.ofNullable(loginUser).map(LoginContext::getUserId).orElse(null);
                    if (StrUtil.isBlank(userId)) {
                        userId = "0";
                    }
                    if (clazz.isAssignableFrom(Integer.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateId(), Integer.class, Integer.parseInt(userId));
                    } else if (clazz.isAssignableFrom(Long.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateId(), Long.class, Long.parseLong(userId));
                    } else if (clazz.isAssignableFrom(String.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateId(), String.class, userId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getCreateId());
        }
    }


    private void fillCreateName(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getCreateName())) {
                Object fieldValue = metaObject.getValue(fieldAutoFillProperties.getCreateName());
                if (fieldValue == null) {
                    LoginContext loginUser = LoginContextHolder.getLoginContext(LoginContext.class);
                    String userName = Optional.ofNullable(loginUser).map(LoginContext::getUserName).orElse(null);
                    if (StrUtil.isEmpty(userName)) {
                        userName = "system";
                    }
                    if (StrUtil.isNotBlank(userName)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getCreateName(), String.class, userName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getCreateName());
        }
    }

    private void fillModifyTime(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getModifyTime())) {
                Class<?> setterType = metaObject.getSetterType(fieldAutoFillProperties.getModifyTime());
                if (setterType.isAssignableFrom(LocalDateTime.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyTime(), LocalDateTime.class, LocalDateTime.now());
                } else if (setterType.isAssignableFrom(Date.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyTime(), Date.class, new Date());
                } else if (setterType.isAssignableFrom(LocalDate.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyTime(), LocalDate.class, LocalDate.now());
                } else if (setterType.isAssignableFrom(Long.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyTime(), Long.class,LocalDateTime.now().toInstant(ZoneOffset.of(fieldAutoFillProperties.getTimeZone())).toEpochMilli());
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getModifyTime());
        }
    }

    private void fillModifyId(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getModifyId())) {
                Class<?> clazz = metaObject.getSetterType(fieldAutoFillProperties.getModifyId());
                LoginContext loginUser = LoginContextHolder.getLoginContext(LoginContext.class);
                String userId = Optional.ofNullable(loginUser).map(LoginContext::getUserId).orElse(null);
                if (StrUtil.isBlank(userId)) {
                    userId = "0";
                }
                if (clazz.isAssignableFrom(Integer.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyId(), Integer.class, Integer.parseInt(userId));
                } else if (clazz.isAssignableFrom(Long.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyId(), Long.class, Long.parseLong(userId));
                } else if (clazz.isAssignableFrom(String.class)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyId(), String.class, userId);
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getModifyId());
        }
    }

    private void fillModifyName(MetaObject metaObject) {
        try {
            if (metaObject.hasGetter(fieldAutoFillProperties.getModifyName())) {
                LoginContext loginUser = LoginContextHolder.getLoginContext(LoginContext.class);
                String userName = Optional.ofNullable(loginUser).map(LoginContext::getUserName).orElse(null);
                if (StrUtil.isEmpty(userName)) {
                    userName = "system";
                }
                if (StrUtil.isNotBlank(userName)) {
                    this.strictInsertFill(metaObject, fieldAutoFillProperties.getModifyName(), String.class, userName);
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getModifyName());
        }
    }

    private void fillTenantId(MetaObject metaObject) {
        try {
            if (tenantProperties.isEnabled() && metaObject.hasGetter(fieldAutoFillProperties.getTenantId())) {
                Object fieldValue = metaObject.getValue(fieldAutoFillProperties.getTenantId());
                if (fieldValue == null) {
                    TableInfo tableInfo = TableInfoHelper.getTableInfo(metaObject.getOriginalObject().getClass());
                    boolean match = tenantProperties.getIgnoreTables().stream().anyMatch(
                            (t) -> t.equalsIgnoreCase(tableInfo.getTableName())
                    );
                    if (match) {
                        return;
                    }
                    Class<?> clazz = metaObject.getSetterType(fieldAutoFillProperties.getTenantId());
                    LoginContext loginUser = LoginContextHolder.getLoginContext(LoginContext.class);
                    Long tenantId = Optional.ofNullable(loginUser).map(LoginContext::getTenantId).orElse(null);
                    if (tenantId == null) {
                        throw new SystemException(FocoErrorCode.TENANT_ID_NOT_EXIT);
                    }
                    if (clazz.isAssignableFrom(Integer.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getTenantId(), Integer.class, tenantId.intValue());
                    } else if (clazz.isAssignableFrom(Long.class)) {
                        this.strictInsertFill(metaObject, fieldAutoFillProperties.getTenantId(), Long.class, tenantId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动填充数据失败，表没有列：{}", fieldAutoFillProperties.getTenantId());
        }
    }
}
