package com.foco.boot.db.intercept.encrypt;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/08/04 19:03
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Slf4j
public class DecryptInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object resultObject = invocation.proceed();
        try {
            if (Objects.isNull(resultObject)) {
                return null;
            }
            if (resultObject instanceof ArrayList) {
                ArrayList resultList = (ArrayList) resultObject;
                if (!CollectionUtils.isEmpty(resultList) && needToDecrypt(resultList.get(0))) {
                    for (Object result : resultList) {
                        decrypt(result);
                    }
                }
            } else {
                if (needToDecrypt(resultObject)) {
                    FiledEncryptUtil.decrypt((String) resultObject);
                }
            }
            return resultObject;
        } catch (Exception e) {
            log.error("解密失败", e);
        }
        return resultObject;
    }

    private boolean needToDecrypt(Object object) {
        Class<?> objectClass = object.getClass();
        EncryptData encryptData = AnnotationUtils.findAnnotation(objectClass, EncryptData.class);
        return Objects.nonNull(encryptData);
    }
    public <T> T decrypt(T result) throws IllegalAccessException {
        Class<?> resultClass = result.getClass();
        Field[] declaredFields = resultClass.getDeclaredFields();
        for (Field field : declaredFields) {
            EncryptField sensitiveField = field.getAnnotation(EncryptField.class);
            if (!Objects.isNull(sensitiveField)) {
                field.setAccessible(true);
                Object object = field.get(result);
                if (object instanceof String) {
                    String value = (String) object;
                    field.set(result, FiledEncryptUtil.decrypt(value));
                }
            }
        }
        return result;
    }
}
