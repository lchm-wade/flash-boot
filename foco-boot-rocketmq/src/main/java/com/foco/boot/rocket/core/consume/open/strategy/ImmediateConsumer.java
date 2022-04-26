package com.foco.boot.rocket.core.consume.open.strategy;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.core.consume.open.RocketResolveApi;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.tools.OpenMessageUtils;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import com.foco.mq.model.BaseConsumerProperty;
import com.foco.mq.model.Msg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author ChenMing
 * @date 2021/11/1
 */
@Slf4j
public class ImmediateConsumer implements RocketResolveApi {

    @Resource
    protected ConfigurableListableBeanFactory beanFactory;

    @Override
    public String strategy() {
        return RocketConsumerStrategy.IMMEDIATE;
    }

    @Override
    public MQConsumer consumer(Object obj, Method method, RocketConsumer consumer) {
        DefaultMQPushConsumer pushConsumer = getPushConsumer(consumer);
        duplicateCode(pushConsumer, obj, method, consumer);
        return pushConsumer;
    }

    @Override
    public MQConsumer consumer(Object obj, Method method, BaseConsumerProperty property) {
        DefaultMQPushConsumer pushConsumer = getPushConsumer((RocketConsumerProperty) property);
        duplicateCode(pushConsumer, obj, method, property.getConsumer());
        return pushConsumer;
    }

    public void duplicateCode(DefaultMQPushConsumer pushConsumer, Object obj, Method method, Annotation consumer) {
        pushConsumer.registerMessageListener(new ImmediateListener(consumer,
                new AbstractConsumerFunction() {
                    @Override
                    public void targetConsume(Msg message) throws RuntimeException, InvocationTargetException, IllegalAccessException {
                        method.invoke(obj, parseParams(method.getParameters(), message));
                    }
                }
        ));
    }

    public class ImmediateListener implements MessageListenerConcurrently {

        private final Annotation annotation;

        private final AbstractConsumerFunction strategy;

        /**
         * 创建监听
         *
         * @param annotation 注解
         * @param strategy   定义消费逻辑
         */
        public ImmediateListener(Annotation annotation, AbstractConsumerFunction strategy) {
            this.annotation = annotation;
            this.strategy = strategy;
        }


        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
            try {
                for (MessageExt msg : msgs) {
                    strategy.consume(OpenMessageUtils.getMsg(msg), annotation);
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Throwable e) {
                log.error("消息消费异常",e);
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }

    }
}
