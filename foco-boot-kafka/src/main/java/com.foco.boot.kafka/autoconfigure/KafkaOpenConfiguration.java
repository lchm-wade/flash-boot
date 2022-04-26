package com.foco.boot.kafka.autoconfigure;

import com.foco.boot.kafka.core.KafkaMessageTransmitter;
import com.foco.boot.kafka.core.consume.ConcurrencyKafkaConsumer;
import com.foco.boot.kafka.core.consume.OrderlyKafkaConsumer;
import com.foco.boot.kafka.core.consume.KafkaConsumerResolve;
import com.foco.boot.kafka.core.consume.KafkaResolveApi;
import com.foco.boot.kafka.model.KafkaMsgConverter;
import com.foco.boot.kafka.properties.KafkaProperties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnClass(KafkaProducer.class)
public class KafkaOpenConfiguration {

    @Bean
    @ConditionalOnMissingBean(KafkaConsumerResolve.class)
    public KafkaConsumerResolve kafkaConsumerResolve(ConfigurableListableBeanFactory beanFactory) {
        return new KafkaConsumerResolve(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean(KafkaMsgConverter.class)
    public KafkaMsgConverter kafkaMsgConverter() {
        return new KafkaMsgConverter();
    }

    @Bean
    public KafkaResolveApi orderlyKafkaConsumer() {
        return new OrderlyKafkaConsumer();
    }

    @Bean
    public KafkaResolveApi concurrencyKafkaConsumer() {
        return new ConcurrencyKafkaConsumer();
    }

    @Bean
    @ConditionalOnMissingBean(KafkaMessageTransmitter.class)
    public KafkaMessageTransmitter kafkaMessageTransmitter(KafkaProperties kafkaProperties){
        return new KafkaMessageTransmitter(kafkaProperties);
    }

}
