package com.foco.boot.dynamic.source;

import cn.hutool.core.collection.CollectionUtil;
import com.foco.boot.dynamic.source.properties.DynamicProperties;
import com.foco.properties.SystemConfig;
import com.foco.context.core.DataSourceContextHolder;
import com.foco.model.constant.AopOrderConstants;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/4 14:35
 **/
@Aspect
@Order(AopOrderConstants.DYNAMIC_DATTA_SOURCE)
@Slf4j
public class DynamicDattaSourceAspect {
    private List<String> readMethod= Lists.newArrayList("select","get","find","query","count","list","pageList","customList");
    @Around("@annotation(com.foco.boot.dynamic.source.DataSource)||execution(* *..*.*ServiceImpl.*(..))")
    public Object changeDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        boolean shouldDo=false;
        if(method.isAnnotationPresent(DataSource.class)){
            String dbId = method.getAnnotation(DataSource.class).value();
            if (!DataSourceContextHolder.containsDataSource(dbId)) {
                log.error("数据源:{},不存在使用主数据源 ->{} " ,dbId, joinPoint.getSignature());
            } else {
                log.info("切换数据源:{}",dbId);
                DataSourceContextHolder.clearDataSource();
                DataSourceContextHolder.setDataSource(dbId);
                shouldDo=true;
            }
        }else {
            if(DynamicProperties.getConfig().getReadWriteSplitting()){
                String methodName = joinPoint.getSignature().getName();
                //开启读写分离
                for(String methodNames:readMethod){
                    if(methodName.startsWith(methodNames)){
                        shouldDo=true;
                        break;
                    }
                }
                if(CollectionUtil.isNotEmpty(DynamicProperties.getConfig().getReadMethod())&&!shouldDo){
                    for(String methodNames:DynamicProperties.getConfig().getReadMethod()){
                        if(methodName.startsWith(methodNames)){
                            shouldDo=true;
                            break;
                        }
                    }
                }
                if(shouldDo){
                    DataSourceContextHolder.clearDataSource();
                    DataSourceContextHolder.setDataSource(DataSourceContextHolder.getDataSources().get(1));
                }
            }
        }
        try {
            return joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable throwable) {
            throw throwable;
        }finally {
            if(shouldDo){
                DataSourceContextHolder.clearDataSource();
            }
        }
    }
}
