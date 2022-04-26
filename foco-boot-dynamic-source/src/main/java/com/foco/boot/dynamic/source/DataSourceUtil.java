package com.foco.boot.dynamic.source;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/07/29 11:19
 */

import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class DataSourceUtil {
    private static Collection<Class<?>> generalClassType;

    public static DataSource getDataSource(String dataSourceClassName, Map<String, Object> dataSourceProperties) throws ReflectiveOperationException {
        DataSource result = (DataSource) Class.forName(dataSourceClassName).newInstance();
        Set<Entry<String, Object>> entries = dataSourceProperties.entrySet();
        for (Entry<String, Object> entry : entries) {
            callSetterMethod(result, getSetterMethodName(entry.getKey()), null == entry.getValue() ? null : entry.getValue().toString());
        }
        return result;
    }

    private static String getSetterMethodName(String propertyName) {
        return propertyName.contains("-") ? CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, "set-" + propertyName) : "set" + String.valueOf(propertyName.charAt(0)).toUpperCase() + propertyName.substring(1, propertyName.length());
    }

    private static void callSetterMethod(DataSource dataSource, String methodName, String setterValue) {
        Iterator iterator = generalClassType.iterator();

        while (iterator.hasNext()) {
            Class each = (Class) iterator.next();
            try {
                Method method = dataSource.getClass().getMethod(methodName, each);
                if (Boolean.TYPE != each && Boolean.class != each) {
                    if (Integer.TYPE != each && Integer.class != each) {
                        if (Long.TYPE != each && Long.class != each) {
                            method.invoke(dataSource, setterValue);
                        } else {
                            method.invoke(dataSource, Long.parseLong(setterValue));
                        }
                    } else {
                        method.invoke(dataSource, Integer.parseInt(setterValue));
                    }
                } else {
                    method.invoke(dataSource, Boolean.valueOf(setterValue));
                }

                return;
            } catch (ReflectiveOperationException var6) {
            }
        }

    }

    private DataSourceUtil() {
    }

    static {
        generalClassType = Sets.newHashSet(new Class[]{Boolean.TYPE, Boolean.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class, String.class});
    }
}

