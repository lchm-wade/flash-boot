package com.foco.boot.db.autoconfigure;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.foco.boot.db.intercept.FocoTenantHandler;
import com.foco.boot.db.intercept.encrypt.DecryptInterceptor;
import com.foco.boot.db.intercept.encrypt.EncryptInterceptor;
import com.foco.boot.db.intercept.log.SqlLogInterceptor;
import com.foco.boot.db.intercept.logicdelete.LogicDeleteInterceptor;
import com.foco.boot.db.intercept.logicdelete.LogicDeleteSelectHandler;
import com.foco.boot.db.intercept.logicdelete.LogicDeleteSqlParser;
import com.foco.boot.db.intercept.security.SafeSqlInterceptor;
import com.foco.boot.db.intercept.security.SafeSqlParser;
import com.foco.boot.db.intercept.type.ListTypeHandler;
import com.foco.boot.db.intercept.CustomMetaObjectHandler;
import com.foco.boot.db.properties.*;
import com.foco.model.constant.DbInterceptorOrderConstants;
import com.foco.model.constant.FocoConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

/**
 * 自定义 MybatisPlus 插件
 *  order值越大越先执行
 * @Author lucoo
 * @Date 2021/6/26 14:14
 */
@Slf4j
@EnableConfigurationProperties({
        LogicDeleteProperties.class,
        SafeSqlProperties.class,
        SqlLogProperties.class,
        FieldAutoFillProperties.class,
        FieldEncryptProperties.class,
        TenantProperties.class,
        MybatisPlusInterceptorProperties.class,
        OptimisticLockerProperties.class})
public class CustomInterceptorAutoConfiguration {
    /**mybatis-plus自动填充字段值*/
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = FieldAutoFillProperties.PREFIX, name = FocoConstants.ENABLED,matchIfMissing = true)
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
    @Bean
    @Order(DbInterceptorOrderConstants.mybatisPlusInterceptor)
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @ConditionalOnProperty(prefix = MybatisPlusInterceptorProperties.PREFIX, name = FocoConstants.ENABLED,matchIfMissing = true)
    public MybatisPlusInterceptor mybatisPlusInterceptor(OptimisticLockerProperties optimisticLockerProperties, TenantProperties tenantProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if(tenantProperties.isEnabled()){
            log.info("开启多租户插件");
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new FocoTenantHandler(tenantProperties)));
        }
        if(optimisticLockerProperties.isEnabled()){
            log.info("开启乐观锁插件");
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }
        return interceptor;
    }
    /**
     * 逻辑删除
     */
    @Order(DbInterceptorOrderConstants.logicDeleteInterceptor)
    @Bean
    @ConditionalOnProperty(prefix = LogicDeleteProperties.PREFIX, name = FocoConstants.ENABLED)
    public LogicDeleteInterceptor logicDeleteInterceptor(LogicDeleteProperties properties) {
        log.info("enabled LogicDelete");
        LogicDeleteSqlParser sqlParser = new LogicDeleteSqlParser(properties);
        sqlParser.setTenantHandler(new LogicDeleteSelectHandler(properties));
        return new LogicDeleteInterceptor(sqlParser);
    }
    /**
     * SQL攻击拦截(全表删除，全表更新)
     */
    @Order(DbInterceptorOrderConstants.safeSqlInterceptor)
    @Bean
    @ConditionalOnProperty(prefix = SafeSqlProperties.PREFIX, name = FocoConstants.ENABLED)
    public SafeSqlInterceptor blockAttackSqlParser(SafeSqlProperties safeSqlProperties) {
        return new SafeSqlInterceptor(new SafeSqlParser(safeSqlProperties));
    }
    /**
     * SQL日志拦截
     */
    @Order(DbInterceptorOrderConstants.sqlLogInterceptor)
    @Bean
    @ConditionalOnProperty(prefix = SqlLogProperties.PREFIX, name = FocoConstants.ENABLED)
    public SqlLogInterceptor sqlLogInterceptor(SqlLogProperties sqlLogProperties) {
        return new SqlLogInterceptor(sqlLogProperties);
    }
    @Bean
    ListTypeHandler listTypeHandler(){
        return new ListTypeHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = FieldEncryptProperties.PREFIX, name = FocoConstants.ENABLED)
    @Order(DbInterceptorOrderConstants.decryptInterceptor)
    DecryptInterceptor decryptInterceptor(){
        return new DecryptInterceptor();
    }
    @Bean
    @ConditionalOnProperty(prefix = FieldEncryptProperties.PREFIX, name = FocoConstants.ENABLED)
    @Order(DbInterceptorOrderConstants.encryptInterceptor)
    EncryptInterceptor encryptInterceptor(){
        return new EncryptInterceptor();
    }
}
