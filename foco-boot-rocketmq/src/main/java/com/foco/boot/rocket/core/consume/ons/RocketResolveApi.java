package com.foco.boot.rocket.core.consume.ons;

import com.aliyun.openservices.ons.api.Admin;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.PropertyValueConst;
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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Properties;

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
    Admin consumer(Object obj, Method method, RocketConsumer consumer);

    /**
     * 定义好的消费者
     *
     * @param obj      目标对象
     * @param method   目标方法
     * @param property consumerId映射的配置信息
     * @return 定义好的消费者
     */
    Admin consumer(Object obj, Method method, BaseConsumerProperty property);

    /**
     * 获取一个公共的配置属性
     * 废弃，请使用{@link #getProperties(RocketConsumer)}
     *
     * @param beanFactory 工厂bean
     * @param consumer    消费者
     * @return 公共的配置属性
     */
    @Deprecated
    default Properties getProperties(ConfigurableListableBeanFactory beanFactory, RocketConsumer consumer) {
        return getProperties(consumer);
    }

    /**
     * 获取一个公共的配置属性
     *
     * @param consumer 消费者
     * @return 公共的配置属性
     */
    default Properties getProperties(RocketConsumer consumer) {
        RocketConsumerProperty consumerProperty = new RocketConsumerProperty();
        consumerProperty.initialize(consumer);
        return getProperties(consumerProperty);
    }

    /**
     * 获取一个公共的配置属性
     *
     * @param consumer 消费者
     * @return 公共的配置属性
     */
    default Properties getProperties(RocketConsumerProperty consumer) {
        ConfigurableListableBeanFactory beanFactory = ResolveConsumerBeanPostProcessor.getBeanFactory();
        Environment environment = beanFactory.getBean(Environment.class);
        RouteBeforeProcessor routeBeforeProcessor = null;
        MqProperties mqProperties = beanFactory.getBean(MqProperties.class);
        try {
            routeBeforeProcessor = beanFactory.getBean(RouteBeforeProcessor.class);
        } catch (NoSuchBeanDefinitionException ignore) {
            //ignore
        }
        Properties properties = new Properties();
        //多实例
        RocketProperties rocketProperties;
        if (!StringUtils.isEmpty(consumer.getServerId())) {
            MqServerPropertiesManager mqServerPropertiesManager = beanFactory.getBean(MqServerPropertiesManager.class);
            String serverId = environment.resolveRequiredPlaceholders(consumer.getServerId());
            AbstractMqServerProperties serverProperties = mqServerPropertiesManager.getServerProperties(serverId);
            if (serverProperties instanceof RocketProperties) {
                rocketProperties = (RocketProperties) serverProperties;
            } else {
                throw new UnsupportedOperationException("serverId" + serverId + "映射的配置类并非RocketMq配置项");
            }
        } else {
            rocketProperties = beanFactory.getBean(RocketProperties.class);
        }
        // 您在控制台创建的Group ID。
        // AccessKey ID阿里云身份验证，在阿里云RAM控制台创建。
        properties.put(PropertyKeyConst.AccessKey, rocketProperties.getAccessKey());
        // Accesskey Secret阿里云身份验证，在阿里云服RAM控制台创建。
        properties.put(PropertyKeyConst.SecretKey, rocketProperties.getSecretKey());
        // 设置TCP接入域名，进入控制台的实例详情页面的TCP协议客户端接入点区域查看。
        properties.put(PropertyKeyConst.NAMESRV_ADDR, rocketProperties.getNamesrvAddr());
        properties.put(PropertyKeyConst.ConsumeTimeout, consumer.getConsumeTimeout());
        properties.put(PropertyKeyConst.ConsumeThreadNums, consumer.getConsumeThread());
        // 订阅方式
        properties.put(PropertyKeyConst.MessageModel, consumer.getConsumeMode() == ConsumeMode.RADIO ? PropertyValueConst.BROADCASTING : PropertyValueConst.CLUSTERING);
        properties.put(PropertyKeyConst.ConsumeMessageBatchMaxSize, consumer.getConsumeMessageBatchMaxSize());
        properties.put(PropertyKeyConst.MaxReconsumeTimes, consumer.getMaxReconsumeTimes());
        properties.put(PropertyKeyConst.MAX_BATCH_MESSAGE_COUNT, consumer.getPullBatchSize());
        String consumerGroup = environment.resolveRequiredPlaceholders(consumer.getConsumerGroup());
        if (routeBeforeProcessor != null) {
            if (mqProperties.isLabelRoute()) {
                consumerGroup = routeBeforeProcessor.getRoute() + consumerGroup;
            }
        }
        properties.put(PropertyKeyConst.GROUP_ID, consumerGroup);
        return properties;
    }
}
