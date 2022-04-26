package com.foco.boot.rocket.core.consume.ons;

import com.aliyun.openservices.ons.api.Admin;
import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.mq.extend.ConsumerEventListener;
import com.foco.mq.extend.ConsumerResolve;
import com.foco.mq.model.BaseConsumerProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ChenMing
 * @date 2021/11/8
 */
@Slf4j
public class RocketConsumerResolve implements ConsumerResolve<RocketConsumer,Admin> {

    private final Environment environment;

    private final List<RocketConsumer> annotations = new ArrayList<>();

    /**
     * key：{@link RocketResolveApi#strategy()}
     */
    private final Map<String, RocketResolveApi> resolveApiMap = new HashMap<>();

    private final List<Admin> admins = new ArrayList<>();

    private final List<ConsumerEventListener> listeners = new ArrayList<>();

    public RocketConsumerResolve(ConfigurableListableBeanFactory beanFactory) {
        this.environment = beanFactory.getBean(Environment.class);
        Map<String, ConsumerEventListener> listeners = beanFactory.getBeansOfType(ConsumerEventListener.class);
        if (!CollectionUtils.isEmpty(listeners)) {
            this.listeners.addAll(listeners.values());
        }
        Map<String, RocketResolveApi> resolveApiMap = beanFactory.getBeansOfType(RocketResolveApi.class);
        resolveApiMap.values().forEach(resolveApi -> this.resolveApiMap.put(resolveApi.strategy(), resolveApi));
    }

    public List<RocketConsumer> getAnnotations() {
        return annotations;
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
        Assert.isTrue(resolveApi != null, "不存在的策略：" + consumer.strategy());
        Admin admin = resolveApi.consumer(obj, method, consumer);
        duplicateCode(admin, consumerGroup);
    }

    @Override
    public boolean resolveConsumer(Object obj, Method method, BaseConsumerProperty property) {
        if (property instanceof RocketConsumerProperty) {
            RocketConsumerProperty rocketConsumerProperty = ((RocketConsumerProperty) property);
            RocketResolveApi rocketResolveApi = resolveApiMap.get(rocketConsumerProperty.getStrategy());
            Assert.isTrue(rocketResolveApi != null, "不存在的策略：" + rocketConsumerProperty.getStrategy());
            Admin admin = rocketResolveApi.consumer(obj, method, property);
            duplicateCode(admin, property.getConsumerId());
            return true;
        }
        return false;
    }

    @Override
    public List<Admin> getConsumers() {
        return admins;
    }

    private void duplicateCode(Admin admin, String consumerId) {
        for (ConsumerEventListener listener : listeners) {
            listener.inject(admin, consumerId);
        }
        admin.start();
        admins.add(admin);
    }

    @Override
    public void stop() {
        for (Admin admin : admins) {
            admin.shutdown();
        }
    }
}
