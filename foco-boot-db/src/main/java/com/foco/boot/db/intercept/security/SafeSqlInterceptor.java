package com.foco.boot.db.intercept.security;


import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;


/**
 * 安全SQL拦截
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SafeSqlInterceptor implements Interceptor {

    private ISqlParser parser;

    public SafeSqlInterceptor(ISqlParser parser) {
        this.parser = parser;

    }
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MetaObject metaObject = SystemMetaObject.forObject(PluginUtils.realTarget(invocation.getTarget()));
        MappedStatement mappedStatement=(MappedStatement)invocation.getArgs()[0];
        String originalSql=mappedStatement.getSqlSource().getBoundSql(invocation.getArgs()[1]).getSql();
        this.parser.parser(metaObject, originalSql);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


}
