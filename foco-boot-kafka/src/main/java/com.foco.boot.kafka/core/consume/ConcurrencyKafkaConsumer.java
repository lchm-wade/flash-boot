package com.foco.boot.kafka.core.consume;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.boot.kafka.constant.KafkaConsumerStrategy;
import com.foco.boot.kafka.properties.KafkaProperties;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import com.foco.mq.model.Msg;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public class ConcurrencyKafkaConsumer implements KafkaResolveApi {
    @Resource
    protected ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Override
    public String strategy() {
        return KafkaConsumerStrategy.CONCURRENCY;
    }

    private KafkaConsumer<String, Object> initKafkaConsumer(KafkaMsgConsumer consumer) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrap_servers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumer.consumerGroup());
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProperties.getRequest_timeout_ms());
        /**
         * 对ConsumerRecord消息中key反序列化类,需要和Producer中key序列化类相对应
         */
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        /**
         * 对ConsumerRecord消息中value的反序列化类,需要和Producer中value序列化类相对应
         */
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,kafkaProperties.getEnable_auto_commit());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,kafkaProperties.getMax_poll_records());
        return new KafkaConsumer<String,Object>(props);
    }

    @Override
    public KafkaConsumer consumer(Object obj, Method method, KafkaMsgConsumer consumer) {
        Parameter[] parameters = method.getParameters();
        AbstractConsumerFunction abstractConsumerFunction = new AbstractConsumerFunction() {
            @Override
            public void targetConsume(Msg message) throws RuntimeException, InvocationTargetException, IllegalAccessException {
                method.invoke(obj, parseParams(parameters, message));
            }
        };
        KafkaConsumer<String,Object> kafkaConsumer = initKafkaConsumer(consumer);
        List<PartitionInfo> partitionInfos = kafkaConsumer.partitionsFor(consumer.topic());
        int partitionSize = partitionInfos.size();
        int consumerNumber = consumer.consumerNum();
        if (consumerNumber > partitionSize) {
            consumerNumber = partitionSize;
        } else if (consumerNumber < 1) {
            consumerNumber = 1;
        }
        for (int i = 0; i < consumerNumber; i++) {
            if (i > 0) {
                kafkaConsumer = initKafkaConsumer(consumer);
            }
            ConcurrencyKafkaConsumerManager kafkaConsumerManager = new ConcurrencyKafkaConsumerManager(kafkaConsumer, abstractConsumerFunction, consumer, kafkaProperties.getEnable_auto_commit());
            new Thread(kafkaConsumerManager).start();
        }
        return null;
    }

}
