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
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author ChenMing
 * @date 2021/11/4
 */
@Slf4j
public class ImmediateOrderlyConsumer implements RocketResolveApi {

    @Resource
    protected ConfigurableListableBeanFactory beanFactory;

    @Override
    public MQConsumer consumer(Object obj, Method method, RocketConsumer consumer) {
        DefaultMQPushConsumer mqConsumer = getPushConsumer(consumer);
        duplicateCode(mqConsumer, obj, method, consumer);
        return mqConsumer;
    }

    @Override
    public MQConsumer consumer(Object obj, Method method, BaseConsumerProperty property) {
        DefaultMQPushConsumer mqConsumer = getPushConsumer((RocketConsumerProperty) property);
        duplicateCode(mqConsumer, obj, method, property.getConsumer());
        return mqConsumer;
    }

    private void duplicateCode(DefaultMQPushConsumer pushConsumer, Object obj, Method method, Annotation consumer) {
        pushConsumer.registerMessageListener(new ImmediateOrderlyListener(consumer,
                new AbstractConsumerFunction() {
                    @Override
                    public void targetConsume(Msg message) throws RuntimeException, InvocationTargetException, IllegalAccessException {
                        method.invoke(obj, parseParams(method.getParameters(), message));
                    }
                }
        ));
    }

    @Override
    public String strategy() {
        return RocketConsumerStrategy.IMMEDIATE_ORDERLY;
    }

    public class ImmediateOrderlyListener implements MessageListenerOrderly {

        private final AbstractConsumerFunction strategy;

        private final Annotation annotation;

        /**
         * 创建监听
         *
         * @param annotation 注解
         * @param strategy   定义消费逻辑
         */
        public ImmediateOrderlyListener(Annotation annotation, AbstractConsumerFunction strategy) {
            this.strategy = strategy;
            this.annotation = annotation;
        }

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
            try {
                for (MessageExt msg : msgs) {
                    strategy.consume(OpenMessageUtils.getMsg(msg), annotation);
                }
                return ConsumeOrderlyStatus.SUCCESS;
            } catch (Exception e) {
                log.error("消息消费异常",e);
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }
    }

}
