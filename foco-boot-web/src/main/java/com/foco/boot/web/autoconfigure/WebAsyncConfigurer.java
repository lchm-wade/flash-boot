package com.foco.boot.web.autoconfigure;

import com.foco.context.executor.ThreadLocalExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/10/09 13:47
 */
@Slf4j
@EnableAsync
public class WebAsyncConfigurer extends AsyncConfigurerSupport {
    private final ThreadLocalExecutor threadLocalExecutor;
    WebAsyncConfigurer(ThreadLocalExecutor threadLocalExecutor){
        this.threadLocalExecutor=threadLocalExecutor;
    }
    @Override
    public Executor getAsyncExecutor() {
        return threadLocalExecutor;
    }
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) -> log.error(ex.getMessage(), ex);
    }
}
