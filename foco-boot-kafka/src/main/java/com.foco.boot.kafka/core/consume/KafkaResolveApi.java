package com.foco.boot.kafka.core.consume;


import com.foco.boot.kafka.KafkaMsgConsumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.lang.reflect.Method;
/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public interface KafkaResolveApi {

    String strategy();

    /**
     * 定义好的消费者
     *
     * @param obj      目标对象
     * @param method   目标方法
     * @param consumer 方法标注的注解信息
     * @return 定义好的消费者
     */
    KafkaConsumer consumer(Object obj, Method method, KafkaMsgConsumer consumer);

}
