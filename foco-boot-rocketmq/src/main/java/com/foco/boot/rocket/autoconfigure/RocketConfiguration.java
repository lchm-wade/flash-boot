package com.foco.boot.rocket.autoconfigure;

import com.foco.boot.rocket.core.MessageTransmitterMapping;
import com.foco.boot.rocket.core.RocketPostProcessorConsumer;
import com.foco.boot.rocket.core.consume.RocketConsumerResolveMapping;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.context.util.BootStrapPrinter;
import com.foco.mq.autoconfigure.MqConfiguration;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author ChenMing
 * @date 2021/10/19
 */
@Configuration
@EnableConfigurationProperties(RocketProperties.class)
@AutoConfigureAfter(MqConfiguration.class)
public class RocketConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-rocketmq",this.getClass());
    }
    @Bean
    @ConditionalOnClass(name = "com.foco.mq.idempotent.autoconfigure.IdempotentConfiguration")
    public RocketPostProcessorConsumer rocketPostProcessorConsumer(ConfigurableListableBeanFactory beanFactory) {
        return new RocketPostProcessorConsumer(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean(MessageTransmitterMapping.class)
    public MessageTransmitterMapping focoMessageTransmitterMapping(RocketProperties rocketProperties) {
        return new MessageTransmitterMapping(rocketProperties);
    }

    @Bean
    @ConditionalOnMissingBean(RocketConsumerResolveMapping.class)
    public RocketConsumerResolveMapping focoConsumerResolveMapping(ConfigurableListableBeanFactory beanFactory) {
        return new RocketConsumerResolveMapping(beanFactory);
    }
}
