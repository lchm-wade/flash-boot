package com.foco.boot.db.intercept.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/08/04 18:51
 */

@Slf4j
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
})
public class EncryptInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
            Field parameterField = parameterHandler.getClass().getDeclaredField("parameterObject");
            parameterField.setAccessible(true);
            Object parameterObject = parameterField.get(parameterHandler);
            if (parameterObject != null) {
                Class<?> parameterObjectClass = parameterObject.getClass();
                EncryptData encryptData = AnnotationUtils.findAnnotation(parameterObjectClass, EncryptData.class);
                if (Objects.nonNull(encryptData)) {
                    Field[] declaredFields = parameterObjectClass.getDeclaredFields();
                    encrypt(declaredFields, parameterObject);
                }
            }
            return invocation.proceed();
        } catch (Exception e) {
            log.error("加密失败", e);
        }
        return invocation.proceed();
    }
    public <T> T encrypt(Field[] declaredFields, T paramsObject) throws IllegalAccessException {
        for (Field field : declaredFields) {
            EncryptField encryptField = field.getAnnotation(EncryptField.class);
            if (!Objects.isNull(encryptField)) {
                field.setAccessible(true);
                Object object = field.get(paramsObject);
                if (object instanceof String) {
                    String value = (String) object;
                    field.set(paramsObject, FiledEncryptUtil.encrypt(value));
                }
            }
        }
        return paramsObject;
    }
}
