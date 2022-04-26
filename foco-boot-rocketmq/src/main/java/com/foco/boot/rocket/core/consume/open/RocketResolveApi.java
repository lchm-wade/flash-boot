package com.foco.boot.rocket.core.consume.open;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.enums.ConsumeMode;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.mq.core.MqServerPropertiesManager;
import com.foco.mq.core.consumer.Consumer;
import com.foco.mq.core.consumer.ResolveConsumerBeanPostProcessor;
import com.foco.mq.extend.AbstractMqServerProperties;
import com.foco.mq.extend.impl.RouteBeforeProcessor;
import com.foco.mq.model.BaseConsumerProperty;
import com.foco.mq.properties.MqProperties;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author ChenMing
 * @date 2021/10/29
 */
public interface RocketResolveApi {

    /**
     * 策略名
     *
     * @return 策略名
     * @see RocketConsumerStrategy
     */
    String strategy();

    /**
     * 定义好的消费者
     *
     * @param obj      目标对象
     * @param method   目标方法
     * @param consumer 方法标注的注解信息
     * @return 定义好的消费者
     */
    MQConsumer consumer(Object obj, Method method, RocketConsumer consumer);

    /**
     * 定义好的消费者
     *
     * @param obj      目标对象
     * @param method   目标方法
     * @param property consumerId映射的配置信息
     * @return 定义好的消费者
     */
    MQConsumer consumer(Object obj, Method method, BaseConsumerProperty property);

    /**
     * 提供公共的开源版mq获取推模式的消费者（避免冗余代码，未start）
     *
     * @param consumer 方法上消费者注解内容
     * @return 推模式消费者
     */
    default DefaultMQPushConsumer getPushConsumer(RocketConsumer consumer) {
        RocketConsumerProperty consumerProperty = new RocketConsumerProperty();
        consumerProperty.initialize(consumer);
        return getPushConsumer(consumerProperty);
    }

    /**
     * 提供公共的开源版mq获取推模式的消费者（避免冗余代码，未start）
     *
     * @param consumer 消费者
     * @return 推模式消费者
     */
    default DefaultMQPushConsumer getPushConsumer(RocketConsumerProperty consumer) {
        ConfigurableListableBeanFactory beanFactory = ResolveConsumerBeanPostProcessor.getBeanFactory();
        Environment environment = beanFactory.getBean(Environment.class);
        MqProperties mqProperties = beanFactory.getBean(MqProperties.class);
        RouteBeforeProcessor routeBeforeProcessor = null;
        try {
            routeBeforeProcessor = beanFactory.getBean(RouteBeforeProcessor.class);
        } catch (NoSuchBeanDefinitionException ignore) {
            //ignore
        }
        String consumerGroup = environment.resolveRequiredPlaceholders(consumer.getConsumerGroup());
        //支持Mq标签路由
        if (routeBeforeProcessor != null) {
            if (mqProperties.isLabelRoute()) {
                consumerGroup = routeBeforeProcessor.getRoute() + consumerGroup;
            }
        }
        String namesrvAddr;
        if (!StringUtils.isEmpty(consumer.getServerId())) {
            String serverId = environment.resolveRequiredPlaceholders(consumer.getServerId());
            AbstractMqServerProperties serverProperties = beanFactory.getBean(MqServerPropertiesManager.class).getServerProperties(serverId);
            if (serverProperties instanceof RocketProperties) {
                namesrvAddr = ((RocketProperties) serverProperties).getNamesrvAddr();
            } else {
                throw new UnsupportedOperationException("异常的RocketMq配置项");
            }
        } else {
            RocketProperties rocketProperties = beanFactory.getBean(RocketProperties.class);
            namesrvAddr = rocketProperties.getNamesrvAddr();
        }
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer(consumerGroup);
        pushConsumer.setNamesrvAddr(namesrvAddr);
        pushConsumer.setMessageModel(consumer.getConsumeMode() == ConsumeMode.RADIO ? MessageModel.BROADCASTING : MessageModel.CLUSTERING);
        String[] topics = consumer.getTopic().split(",");
        String[] tags = consumer.getTag().split(",");
        String tagAll = "*";
        for (int i = 0; i < topics.length; i++) {
            String tag = i > tags.length - 1 ? tagAll : environment.resolvePlaceholders(tags[i]);
            try {
                pushConsumer.subscribe(environment.resolveRequiredPlaceholders(topics[i]), tag);
            } catch (MQClientException e) {
                e.printStackTrace();
            }
        }
        pushConsumer.setConsumeTimeout(consumer.getConsumeTimeout());
        pushConsumer.setConsumeThreadMax(consumer.getConsumeThread());
        pushConsumer.setConsumeThreadMin(consumer.getConsumeThread());
        pushConsumer.setMaxReconsumeTimes(consumer.getMaxReconsumeTimes());
        pushConsumer.setPullBatchSize(consumer.getPullBatchSize());
        pushConsumer.setConsumeMessageBatchMaxSize(consumer.getConsumeMessageBatchMaxSize());
        return pushConsumer;
    }
//
//    /**
//     * 将FOCO框架内定义转化为Rocket定义
//     *
//     * @param consumeFromWhere foco的enum
//     * @return rocket的enum
//     */
//    default ConsumeFromWhere convert(com.foco.boot.rocket.enums.ConsumeFromWhere consumeFromWhere) {
//        ConsumeFromWhere result;
//        switch (consumeFromWhere) {
//            case CONSUME_FROM_TIMESTAMP:
//                result = ConsumeFromWhere.CONSUME_FROM_TIMESTAMP;
//                break;
//            case CONSUME_FROM_FIRST_OFFSET:
//                result = ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET;
//                break;
//            case CONSUME_FROM_LAST_OFFSET:
//                result = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
//                break;
//            default:
//                result = ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET;
//        }
//        return result;
//    }
}
