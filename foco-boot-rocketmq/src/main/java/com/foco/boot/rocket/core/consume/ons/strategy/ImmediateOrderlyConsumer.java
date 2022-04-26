package com.foco.boot.rocket.core.consume.ons.strategy;

import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.core.consume.ons.RocketResolveApi;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.tools.OnsMessageUtils;
import com.foco.mq.core.MqServerPropertiesManager;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import com.foco.mq.core.consumer.Consumer;
import com.foco.mq.model.BaseConsumerProperty;
import com.foco.mq.model.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Properties;

/**
 * @author ChenMing
 * @date 2021/11/4
 */
@Slf4j
public class ImmediateOrderlyConsumer implements RocketResolveApi {

    @Resource
    protected Environment environment;

    @Resource
    protected ConfigurableListableBeanFactory beanFactory;

    @Resource
    protected MqServerPropertiesManager manager;

    @Override
    public String strategy() {
        return RocketConsumerStrategy.IMMEDIATE_ORDERLY;
    }

    @Override
    public Admin consumer(Object obj, Method method, RocketConsumer consumer) {
        Properties properties = getProperties(consumer);
        return duplicateCode(obj, method, properties, consumer, consumer.topic(), consumer.tag());
    }

    @Override
    public Admin consumer(Object obj, Method method, BaseConsumerProperty property) {
        RocketConsumerProperty rocketConsumerProperty = (RocketConsumerProperty) property;
        Properties properties = getProperties(rocketConsumerProperty);
        String[] topics = rocketConsumerProperty.getTopic().split(",");
        String[] tags = rocketConsumerProperty.getTag().split(",");
        return duplicateCode(obj, method, properties, property.getConsumer(), topics, tags);
    }

    private Admin duplicateCode(Object obj, Method method, Properties properties, Annotation consumer, String[] topics, String[] tags) {
        OrderConsumer admin = ONSFactory.createOrderedConsumer(properties);
        Parameter[] parameters = method.getParameters();
        ImmediateOrderlyListener listener = new ImmediateOrderlyListener(consumer,
                new AbstractConsumerFunction() {
                    @Override
                    public void targetConsume(Msg message) throws RuntimeException, InvocationTargetException, IllegalAccessException {
                        method.invoke(obj, parseParams(parameters, message));
                    }
                }
        );
        String tagAll = "*";
        for (int i = 0; i < topics.length; i++) {
            String tag = i > tags.length - 1 ? tagAll : environment.resolvePlaceholders(tags[i]);
            admin.subscribe(environment.resolveRequiredPlaceholders(topics[i]), tag, listener);
        }
        return admin;
    }

    public class ImmediateOrderlyListener implements MessageOrderListener {

        private final AbstractConsumerFunction strategy;

        private final Annotation annotation;

        /**
         * 创建监听
         *
         * @param annotation 注解
         * @param strategy   定义消费逻辑
         */
        ImmediateOrderlyListener(Annotation annotation, AbstractConsumerFunction strategy) {
            this.strategy = strategy;
            this.annotation = annotation;
        }

        @Override
        public OrderAction consume(Message message, ConsumeOrderContext context) {
            try {
                strategy.consume(OnsMessageUtils.getMsg(message), annotation);
                return OrderAction.Success;
            } catch (Exception e) {
                log.error("消息消费异常",e);
                return OrderAction.Suspend;
            }
        }
    }
}
