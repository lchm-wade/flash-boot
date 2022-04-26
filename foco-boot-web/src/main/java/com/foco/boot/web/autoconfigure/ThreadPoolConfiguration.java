package com.foco.boot.web.autoconfigure;
import com.alibaba.fastjson.JSON;
import com.foco.boot.web.executor.BootThreadLocalExecutor;
import com.foco.boot.web.properties.ThreadPoolProperties;
import com.foco.context.annotation.ConditionalOnMissingCloud;
import com.foco.context.executor.ThreadLocalExecutor;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.constant.MainClassConstant;
import com.foco.model.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
/**
 * description: 开启异步任务的支持
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
@Slf4j
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class ThreadPoolConfiguration{
    @Bean
    @Primary
    ThreadPoolTaskExecutor createThreadPoolTaskExecutor(ThreadPoolProperties threadPoolProperties) {
        //cpu 核心数
        int cpuCoreCount = Runtime.getRuntime().availableProcessors();
        log.info("cpuCoreCount is " + cpuCoreCount);
        int maxPoolSize = threadPoolProperties.getMaxPoolSize();
        int corePoolSize = threadPoolProperties.getCorePoolSize();
        int keepAliveSeconds = threadPoolProperties.getKeepAliveSeconds();
        if (corePoolSize < 0 ||
                maxPoolSize <= 0 ||
                maxPoolSize < corePoolSize ||
                keepAliveSeconds < 0){
            SystemException.throwException(FocoErrorCode.CONFIG_VALID.getCode(),String.format("线程参数配置不合法:%s",JSON.toJSONString(threadPoolProperties)));
        }
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        threadPoolTaskExecutor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        threadPoolTaskExecutor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        threadPoolTaskExecutor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        try {
            threadPoolTaskExecutor.setRejectedExecutionHandler(threadPoolProperties.getRejectedExecutionHandler().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());
        threadPoolTaskExecutor.afterPropertiesSet();
        return threadPoolTaskExecutor;
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingCloud
    public ThreadLocalExecutor createBootThreadLocalExecutor(ThreadPoolTaskExecutor executor) {
        return new BootThreadLocalExecutor(executor);
    }
}
