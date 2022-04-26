package com.foco.boot.rocket.core.consume.ons.strategy;

import com.aliyun.openservices.ons.api.*;
import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.core.consume.ons.RocketResolveApi;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.tools.OnsMessageUtils;
import com.foco.mq.core.MqServerPropertiesManager;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
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
 * @date 2021/11/1
 */
@Slf4j
public class ImmediateConsumer implements RocketResolveApi {

    @Resource
    protected Environment environment;

    @Resource
    protected ConfigurableListableBeanFactory beanFactory;

    @Override
    public String strategy() {
        return RocketConsumerStrategy.IMMEDIATE;
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


    private Admin duplicateCode(Object obj, Method method, Properties properties, Annotation consumer, String[] topic, String[] tags) {
        Parameter[] parameters = method.getParameters();
        ImmediateListener listener = new ImmediateListener(consumer,
                new AbstractConsumerFunction() {
                    @Override
                    public void targetConsume(Msg message) throws RuntimeException, InvocationTargetException, IllegalAccessException {
                        method.invoke(obj, parseParams(parameters, message));
                    }
                }
        );
        Consumer admin = ONSFactory.createConsumer(properties);
        String tagAll = "*";
        for (int i = 0; i < topic.length; i++) {
            String tag = i > tags.length - 1 ? tagAll : environment.resolvePlaceholders(tags[i]);
            admin.subscribe(environment.resolveRequiredPlaceholders(topic[i]), tag, listener);
        }
        return admin;
    }

    /**
     * 商业版内部实现是并发消费
     */
    public class ImmediateListener implements MessageListener {

        private final Annotation annotation;

        private final AbstractConsumerFunction strategy;

        /**
         * 创建监听
         *
         * @param annotation 注解
         * @param strategy   定义消费逻辑
         */
        ImmediateListener(Annotation annotation, AbstractConsumerFunction strategy) {
            this.annotation = annotation;
            this.strategy = strategy;
        }

        @Override
        public Action consume(Message message, ConsumeContext context) {
            try {
                strategy.consume(OnsMessageUtils.getMsg(message), annotation);
                return Action.CommitMessage;
            } catch (Throwable e) {
                log.error("消息消费异常",e);
                return Action.ReconsumeLater;
            }
        }
    }

}
