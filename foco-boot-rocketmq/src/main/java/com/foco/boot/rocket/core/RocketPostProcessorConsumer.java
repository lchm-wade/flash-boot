package com.foco.boot.rocket.core;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.enums.ConsumeMode;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.model.exception.SystemException;
import com.foco.mq.constant.MqConstant;
import com.foco.mq.constant.MsgPropertyConstant;
import com.foco.mq.core.MqServerPropertiesManager;
import com.foco.mq.core.consumer.Consumer;
import com.foco.mq.extend.ConsumeBeforeProcessorConsumer;
import com.foco.mq.extend.impl.RouteBeforeProcessor;
import com.foco.mq.model.BaseConsumerProperty;
import com.foco.mq.model.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author ChenMing
 * @date 2021/11/7
 */
public class RocketPostProcessorConsumer implements ConsumeBeforeProcessorConsumer {

    private static String ipPort;

    @Resource
    private Environment environment;

    @Resource
    private MqServerPropertiesManager manager;

    @Autowired(required = false)
    private RouteBeforeProcessor processor;

    public RocketPostProcessorConsumer(ConfigurableListableBeanFactory beanFactory) {
        try {
            if (StringUtils.isEmpty(ipPort)) {
                ipPort = InetAddress.getLocalHost().getHostAddress() + ":" + beanFactory.getBean(Environment.class).getProperty("server.port");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new SystemException("ip获取异常：" + e.getMessage());
        }
    }

    @Override
    public Msg postProcessBeforeConsumer(Msg msg, Annotation annotation) {
        if (annotation instanceof RocketConsumer) {
            RocketConsumer consumer = (RocketConsumer) annotation;
            duplicateCode(msg, consumer.consumeMode(), consumer.consumerGroup(), consumer.packageTransaction(), consumer.idempotent());
        } else if (annotation instanceof Consumer) {
            BaseConsumerProperty consumer = manager.getConsumer(((Consumer) annotation).value());
            if (consumer instanceof RocketConsumerProperty) {
                RocketConsumerProperty rocketConsumerProperty = (RocketConsumerProperty) consumer;
                duplicateCode(msg, rocketConsumerProperty.getConsumeMode(), rocketConsumerProperty.getConsumerGroup()
                        , rocketConsumerProperty.isPackageTransaction(), rocketConsumerProperty.isIdempotent());
            }
        }
        return msg;
    }

    private void duplicateCode(Msg msg, ConsumeMode consumeMode, String group, boolean packageTransaction, boolean idempotent) {
        String idCard;
        switch (consumeMode) {
            case RADIO:
                idCard = ipPort;
                break;
            case CLUSTER:
                String consumerGroup = environment.resolveRequiredPlaceholders(group);
                if (processor != null) {
                    consumerGroup = processor.getLocalRoute() + consumerGroup;
                }
                idCard = consumerGroup;
                break;
            default:
                final String errorMsg = "消费模式异常,当前消费模式未命中：" + consumeMode;
                throw new SystemException(errorMsg);
        }
        msg.put(MsgPropertyConstant.ID_CARD, idCard);
        msg.put(MsgPropertyConstant.PACKAGE_TRANSACTION, String.valueOf(packageTransaction));
        msg.put(MsgPropertyConstant.IDEMPOTENT, String.valueOf(idempotent));
    }

    @Override
    public int consumeOrder() {
        return MqConstant.IDEMPOTENT_BEFORE_PROCESSOR_WRAP_ORDER - 1;
    }
}
