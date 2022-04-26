package com.foco.boot.web.properties;

import com.foco.properties.AbstractProperties;
import com.foco.model.constant.FocoConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/7/21 9:52
 **/
@Getter
@Setter
@ConfigurationProperties(prefix=ThreadPoolProperties.PREFIX)
public class ThreadPoolProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"thread-pool";
    public static ThreadPoolProperties getConfig(){
        return getConfig(ThreadPoolProperties.class);
    }
    int cpuCoreCount = Runtime.getRuntime().availableProcessors();
    /**
     * 核心线程数
     */
    private int corePoolSize=cpuCoreCount;
    /**
     * 队列容量
     */
    private int queueCapacity=cpuCoreCount*50;
    /**
     * 最大线程数
     */
    private int maxPoolSize=cpuCoreCount * 3;
    /**
     * 线程保活时间
     */
    private int KeepAliveSeconds=300;
    private String threadNamePrefix="foco-thread-";
    private Class<? extends RejectedExecutionHandler> rejectedExecutionHandler= ThreadPoolExecutor.CallerRunsPolicy.class;
}
