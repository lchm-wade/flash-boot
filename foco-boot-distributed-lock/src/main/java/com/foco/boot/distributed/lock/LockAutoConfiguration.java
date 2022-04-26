package com.foco.boot.distributed.lock;

import com.foco.boot.distributed.lock.redis.RedisDistributedLock;
import com.foco.boot.distributed.lock.redis.RedissionAutoConfiguration;
import com.foco.context.util.BootStrapPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @Description: TODO
 * @Author lucoo
 * @Date 2021/1/4 11:51
 **/
@Configuration
@Import(RedissionAutoConfiguration.class)
@Slf4j
public class LockAutoConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-distributed-lock",this.getClass());
    }
    @Bean
    public ILock lock(){
        return new RedisDistributedLock();
    }
}
