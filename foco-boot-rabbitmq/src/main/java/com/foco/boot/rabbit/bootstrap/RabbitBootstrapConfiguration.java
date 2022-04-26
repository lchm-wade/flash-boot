package com.foco.boot.rabbit.bootstrap;



import com.foco.boot.rabbit.configuration.RabbitMQConfiguration;
import com.foco.boot.rabbit.converter.JsonMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
@Slf4j
@Import(RabbitMQConfiguration.class)
public class RabbitBootstrapConfiguration {

    @Bean
    public JsonMessageConverter messageConverter() {
        return new JsonMessageConverter();
    }
    @PostConstruct
    private void print() {
        log.info("enabled rabbitmq");
    }
}
