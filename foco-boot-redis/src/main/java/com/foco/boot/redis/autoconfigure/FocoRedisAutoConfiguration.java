package com.foco.boot.redis.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foco.boot.redis.cache.DefaultRedisService;
import com.foco.boot.redis.cache.RedisService;
import com.foco.boot.redis.serializer.KeyStringSerializer;
import com.foco.boot.redis.support.RedisSerialNumber;
import com.foco.context.common.DefaultRedisPrefix;
import com.foco.context.common.RedisPrefix;
import com.foco.context.util.BootStrapPrinter;
import com.foco.context.util.SerialNumberHelper;
import com.foco.properties.CustomRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import javax.annotation.PostConstruct;
import java.time.Duration;


/**
 * description: 缓存redis自动配置
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Slf4j
@EnableCaching
@Configuration
@EnableConfigurationProperties(CustomRedisProperties.class)
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class FocoRedisAutoConfiguration {
    @Autowired
    private RedisProperties redisProperties;
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-redis",this.getClass());
    }
    @Bean
    KeyStringSerializer keyStringSerializer(){
        return new KeyStringSerializer();
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
                                                       ObjectMapper objectMapper,KeyStringSerializer keyStringSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        //key序列化方式
        RedisSerializer<String> defalutSerializer = template.getStringSerializer();
        //值序列化方式
        RedisSerializer<Object> jsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        //设置key 的序列化方式
        template.setKeySerializer(keyStringSerializer);
        template.setHashKeySerializer(keyStringSerializer);
        //设置值 的序列化方式
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);
        //设置默认的序列化方式
        template.setDefaultSerializer(defalutSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                // 设置缓存有效期一小时
                .entryTtl(Duration.ofHours(1));

        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    public RedisService redisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper, RedisPrefix redisPrefix) {
        return new DefaultRedisService(redisTemplate, objectMapper,redisPrefix);
    }
    @Bean
    @ConditionalOnMissingBean(SerialNumberHelper.class)
    SerialNumberHelper serialNumberHelper(){
        return new RedisSerialNumber();
    }

    @Bean(name = "redisIncr")
    @ConditionalOnMissingBean(name = "redisIncr")
    public DefaultRedisScript defaultRedisScript() {
        DefaultRedisScript defaultRedisScript = new DefaultRedisScript<Long>();
        defaultRedisScript.setResultType(Long.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redisIncr.lua")));
        return defaultRedisScript;
    }
}
