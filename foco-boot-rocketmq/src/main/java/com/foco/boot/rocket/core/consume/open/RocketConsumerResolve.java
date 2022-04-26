package com.foco.boot.rocket.core.consume.open;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.mq.extend.ConsumerEventListener;
import com.foco.mq.extend.ConsumerResolve;
import com.foco.mq.model.BaseConsumerProperty;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ChenMing
 * @date 2021/10/19
 */
public class RocketConsumerResolve implements ConsumerResolve<RocketConsumer, MQConsumer> {

    private final Environment environment;

    private final List<RocketConsumer> annotations = new ArrayList<>();

    private final Map<String, RocketResolveApi> resolveApiMap = new HashMap<>();

    private final List<MQConsumer> consumers = new LinkedList<>();

    private final List<ConsumerEventListener> listeners = new ArrayList<>();

    public List<RocketConsumer> getAnnotations() {
        return annotations;
    }

    public RocketConsumerResolve(ConfigurableListableBeanFactory beanFactory) {
        this.environment = beanFactory.getBean(Environment.class);
        Map<String, ConsumerEventListener> listeners = beanFactory.getBeansOfType(ConsumerEventListener.class);
        if (!CollectionUtils.isEmpty(listeners)) {
            this.listeners.addAll(listeners.values());
        }
        Map<String, RocketResolveApi> resolveApiMap = beanFactory.getBeansOfType(RocketResolveApi.class);
        resolveApiMap.values().forEach(resolveApi -> this.resolveApiMap.put(resolveApi.strategy(), resolveApi));
    }

    @Override
    public Class<RocketConsumer> annotation() {
        return RocketConsumer.class;
    }

    @Override
    public void resolveConsumer(Object obj, Method method, RocketConsumer consumer) {
        final String consumerGroup = environment.resolveRequiredPlaceholders(consumer.consumerGroup());
        Set<String> set = annotations.stream().map(RocketConsumer::consumerGroup).collect(Collectors.toSet());
        Assert.isTrue(!set.contains(consumerGroup), "存在重复consumerGroup，方法：" + obj.getClass() + "#" + method.getName());
        RocketResolveApi resolveApi = resolveApiMap.get(consumer.strategy());
        MQConsumer impl = resolveApi.consumer(obj, method, consumer);
        duplicateCode(impl, consumerGroup);
    }

    @Override
    public boolean resolveConsumer(Object obj, Method method, BaseConsumerProperty property) {
        if (property instanceof RocketConsumerProperty) {
            RocketConsumerProperty rocketConsumerProperty = ((RocketConsumerProperty) property);
            RocketResolveApi rocketResolveApi = resolveApiMap.get(rocketConsumerProperty.getStrategy());
            MQConsumer c = rocketResolveApi.consumer(obj, method, rocketConsumerProperty);
            duplicateCode(c, property.getConsumerId());
            return true;
        }
        return false;
    }

    @Override
    public List<MQConsumer> getConsumers() {
        return consumers;
    }

    private void duplicateCode(MQConsumer consumer, String consumerId) {
        for (ConsumerEventListener listener : listeners) {
            listener.inject(consumer, consumerId);
        }
        try {
            if (consumer instanceof MQPushConsumer) {
                ((MQPushConsumer) consumer).start();
            }
            if (consumer instanceof MQPullConsumer) {
                ((MQPullConsumer) consumer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        consumers.add(consumer);
    }

    @Override
    public void stop() {
        for (MQConsumer consumer : consumers) {
            if (consumer instanceof MQPushConsumer) {
                ((MQPushConsumer) consumer).shutdown();
            }
            if (consumer instanceof MQPullConsumer) {
                ((MQPullConsumer) consumer).shutdown();
            }
        }
    }
}
