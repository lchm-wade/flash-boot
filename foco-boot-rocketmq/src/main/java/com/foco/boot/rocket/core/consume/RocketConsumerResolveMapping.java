package com.foco.boot.rocket.core.consume;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.core.consume.ons.RocketConsumerResolve;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.context.asserts.Assert;
import com.foco.mq.constant.MqConstant;
import com.foco.mq.core.MqServerPropertiesManager;
import com.foco.mq.extend.AbstractMqServerProperties;
import com.foco.mq.extend.ConsumerResolve;
import com.foco.mq.model.BaseConsumerProperty;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ChenMing
 * @version 1.0.0
 * @description TODO
 * @date 2022/01/13 10:05
 * @since foco2.3.0
 */
public class RocketConsumerResolveMapping implements ConsumerResolve<RocketConsumer, Object> {

    private RocketConsumerResolve rocketOnsConsumerResolve;

    private com.foco.boot.rocket.core.consume.open.RocketConsumerResolve rocketOpenConsumerResolve;

    private final RocketProperties rocketProperties;

    private final MqServerPropertiesManager manager;

    private final Environment environment;

    public RocketConsumerResolveMapping(ConfigurableListableBeanFactory beanFactory) {
        this.rocketProperties = beanFactory.getBean(RocketProperties.class);
        this.manager = beanFactory.getBean(MqServerPropertiesManager.class);
        this.rocketOnsConsumerResolve = new RocketConsumerResolve(beanFactory);
        this.rocketOpenConsumerResolve = new com.foco.boot.rocket.core.consume.open.RocketConsumerResolve(beanFactory);
        this.environment = beanFactory.getBean(Environment.class);
    }

    @Override
    public Class<RocketConsumer> annotation() {
        return RocketConsumer.class;
    }

    @Override
    public void resolveConsumer(Object obj, Method method, RocketConsumer annotation) {
        if (StringUtils.isEmpty(annotation.serverId())) {
            if (rocketProperties.isUseOpen()) {
                rocketOpenConsumerResolve.resolveConsumer(obj, method, annotation);
            } else {
                rocketOnsConsumerResolve.resolveConsumer(obj, method, annotation);
            }
        } else {
            String serverId = environment.resolveRequiredPlaceholders(annotation.serverId());
            AbstractMqServerProperties properties = manager.getServerProperties(serverId);
            Assert.that(properties).isNotNull("未配置的serverId：" + serverId);
            if (properties instanceof RocketProperties) {
                if (((RocketProperties) properties).isUseOpen()) {
                    rocketOpenConsumerResolve.resolveConsumer(obj, method, annotation);
                } else {
                    rocketOnsConsumerResolve.resolveConsumer(obj, method, annotation);
                }
            } else {
                throw new UnsupportedOperationException("serverId：" + serverId + "非Rocket配置，却标注了类型为：" + RocketConsumer.class.getName());
            }
        }
    }

    @Override
    public boolean resolveConsumer(Object obj, Method method, BaseConsumerProperty property) {
        AbstractMqServerProperties properties = manager.getPropertiesByConsumerId(property.getConsumerId());
        Assert.that(properties).isNotNull(MqConstant.NOT_EXIST_CLUE.replace("{}", "consumerId：" + property.getConsumerId()));
        if (properties instanceof RocketProperties) {
            if (((RocketProperties) properties).isUseOpen()) {
                return rocketOpenConsumerResolve.resolveConsumer(obj, method, property);
            } else {
                return rocketOnsConsumerResolve.resolveConsumer(obj, method, property);
            }
        }
        return false;
    }

    @Override
    public List<Object> getConsumers() {
        List<Object> consumers = new ArrayList<>();
        try {
            Class.forName("com.aliyun.openservices.ons.api.Admin");
            List ons = rocketOnsConsumerResolve.getConsumers();
            if (CollectionUtils.isEmpty(ons)) {
                consumers.addAll(ons);
            }
        } catch (ClassNotFoundException e) {
            //忽略，此处仅作可排除开源包设计
        }
        try {
            Class.forName("org.apache.rocketmq.client.consumer.MQConsumer");
            List open = rocketOpenConsumerResolve.getConsumers();
            if (CollectionUtils.isEmpty(open)) {
                consumers.addAll(open);
            }
        } catch (ClassNotFoundException e) {
            //忽略，此处仅作可排除商业包设计
        }
        return consumers;
    }

    public RocketConsumerResolve getRocketOnsConsumerResolve() {
        return rocketOnsConsumerResolve;
    }

    public com.foco.boot.rocket.core.consume.open.RocketConsumerResolve getRocketOpenConsumerResolve() {
        return rocketOpenConsumerResolve;
    }

    @Override
    public void stop() {
        if (rocketOnsConsumerResolve != null) {
            rocketOnsConsumerResolve.stop();
        }
        if (rocketOpenConsumerResolve != null) {
            rocketOpenConsumerResolve.stop();
        }
    }
}
