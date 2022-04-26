package com.foco.boot.db.intercept.log;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.foco.boot.db.properties.SqlLogProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StopWatch;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/29 17:00
 **/
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        ), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})
public class SqlLogInterceptor implements Interceptor {
    private SqlLogProperties sqlLogProperties;

    public SqlLogInterceptor(SqlLogProperties sqlLogProperties) {
        this.sqlLogProperties = sqlLogProperties;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }
        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        if (invocation.getArgs().length == 6) {
            boundSql = (BoundSql) invocation.getArgs()[5];
        }
        String sqlId = mappedStatement.getId();

        Configuration configuration = mappedStatement.getConfiguration();
        try {
            if(log.isInfoEnabled()){
                showSql(configuration, boundSql, sqlId);
            }
        } catch (Exception e) {
            log.error("日志打印异常", e);
        }
        Object value;
        if (sqlLogProperties.isPrintTime()) {
            StopWatch watch = new StopWatch();
            watch.start();
            try {
                value = invocation.proceed();
            } finally {
                watch.stop();
                log.info("sqlId:{},消耗:{}毫秒", sqlId, watch.getTotalTimeMillis());
            }
        } else {
            value = invocation.proceed();
        }
        if (sqlLogProperties.isPrintRsp()&&log.isInfoEnabled()) {
            log.info("响应:{}", JSON.toJSONString(value));
        }
        return value;
    }

    private static void showSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        //替换空格、换行、tab缩进等
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        logs(sql, sqlId);
    }

    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            value = "'" + simpleDateFormat.format(obj) + "'";
        } else if (obj instanceof LocalDate) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof LocalDateTime) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            value = "'" + df.format((LocalDateTime) obj) + "'";
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "NULL";
            }
        }
        return value.replace("$", "\\$");
    }

    private static void logs(String sql, String sqlId) {
            StringBuilder sb = new StringBuilder()
                    .append("sqlId:").append(sqlId)
                    .append(StringPool.NEWLINE).append("最终执行sql:")
                    .append(sql);
            log.info(sb.toString());
    }
}
