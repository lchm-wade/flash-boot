package com.foco.boot.kafka.autoconfigure;

import com.foco.boot.kafka.core.KafkaPostProcessorConsumer;
import com.foco.boot.kafka.properties.KafkaProperties;
import com.foco.context.util.BootStrapPrinter;
import com.foco.mq.autoconfigure.MqConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Configuration
@AutoConfigureAfter(MqConfiguration.class)
public class KafkaConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-kafka",this.getClass());
    }
    @Bean
    @ConditionalOnClass(name = "com.foco.mq.idempotent.autoconfigure.IdempotentConfiguration")
    public KafkaPostProcessorConsumer kafkaPostProcessorConsumer() {
        return new KafkaPostProcessorConsumer();
    }

}
