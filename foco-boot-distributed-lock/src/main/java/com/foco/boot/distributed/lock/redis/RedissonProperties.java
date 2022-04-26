package com.foco.boot.distributed.lock.redis;

import lombok.Data;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonProperties {
    private Config redisson;
}