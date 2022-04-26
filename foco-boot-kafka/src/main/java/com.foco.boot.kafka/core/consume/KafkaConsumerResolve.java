package com.foco.boot.kafka.core.consume;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.mq.extend.ConsumerResolve;
import com.foco.mq.model.BaseConsumerProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Slf4j
public class KafkaConsumerResolve implements ConsumerResolve<KafkaMsgConsumer, Consumer> {

    private final Environment environment;

    private final List<KafkaMsgConsumer> annotations = new ArrayList<>();

    private final Map<String, KafkaResolveApi> resolveApiMap = new HashMap<>();

    public List<KafkaMsgConsumer> getAnnotations() {
        return annotations;
    }

    public KafkaConsumerResolve(ConfigurableListableBeanFactory beanFactory) {
        this.environment = beanFactory.getBean(Environment.class);
        Map<String, KafkaResolveApi> resolveApiMap = beanFactory.getBeansOfType(KafkaResolveApi.class);
        resolveApiMap.values().forEach(resolveApi -> this.resolveApiMap.put(resolveApi.strategy(), resolveApi));
    }

    @Override
    public Class<KafkaMsgConsumer> annotation() {
        return KafkaMsgConsumer.class;
    }

    @Override
    public void resolveConsumer(Object obj, Method method, KafkaMsgConsumer consumer) {
        Set<String> uniqueSet = new HashSet<>();
        annotations.forEach(annotation -> {
            String uniqueKey = annotation.topic() + "#" + annotation.consumerGroup();
            uniqueSet.add(uniqueKey);
        });
        //获取当前注解配置的consumerGroup
        final String consumerGroup = environment.resolveRequiredPlaceholders(consumer.consumerGroup());
        String topic = environment.resolveRequiredPlaceholders(consumer.topic());
        try {
            InvocationHandler h = Proxy.getInvocationHandler(consumer);
            Field hField = h.getClass().getDeclaredField("memberValues");
            hField.setAccessible(true);
            Object memberValuesObj = hField.get(h);
            if (memberValuesObj instanceof Map) {
                Map<String, Object> memberValues = (Map) memberValuesObj;
                memberValues.put("topic", topic);
                memberValues.put("consumerGroup", consumerGroup);
            }
        } catch (Exception e) {
            log.error("set KafkaMsgConsumer params error.", e);
            return;
        }
        Assert.isTrue(!uniqueSet.contains(topic + "#" + consumerGroup), "一个topic存在多个重复consumerGroup，方法：" + obj.getClass() + "#" + method.getName());
        annotations.add(consumer);
        KafkaResolveApi resolveApi = resolveApiMap.get(consumer.strategy());
        resolveApi.consumer(obj, method, consumer);
    }

    @Override
    public boolean resolveConsumer(Object obj, Method method, BaseConsumerProperty property) {
        //暂时忽略，kafka未提供的能力
        return false;
    }

    @Override
    public List<Consumer> getConsumers() {
        //暂时忽略，kafka未提供的能力
        return null;
    }

    @Override
    public void stop() {

    }
}
