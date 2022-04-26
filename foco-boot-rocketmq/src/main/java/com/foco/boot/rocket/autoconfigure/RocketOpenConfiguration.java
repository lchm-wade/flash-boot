package com.foco.boot.rocket.autoconfigure;

import com.foco.boot.rocket.core.consume.RocketConsumerResolveMapping;
import com.foco.boot.rocket.core.consume.open.RocketResolveApi;
import com.foco.boot.rocket.core.consume.open.strategy.ImmediateConsumer;
import com.foco.boot.rocket.core.consume.open.strategy.ImmediateOrderlyConsumer;
import com.foco.boot.rocket.core.consume.open.DelayConsumerRegister;
import com.foco.boot.rocket.model.RocketMsgConverter;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.mq.MsgProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 开源版本
 *
 * @author ChenMing
 * @date 2021/11/8
 */
@Configuration
@ConditionalOnClass(Message.class)
public class RocketOpenConfiguration {
    @Bean
    @ConditionalOnMissingBean(RocketMsgConverter.class)
    public RocketMsgConverter rocketMsgConverter() {
        return new RocketMsgConverter();
    }

    @Bean
    public RocketResolveApi immediateConsumer() {
        return new ImmediateConsumer();
    }

    @Bean
    public RocketResolveApi immediateOrderlyConsumer() {
        return new ImmediateOrderlyConsumer();
    }

    @Bean
    public DelayConsumerRegister delayConsumer(MsgProducer msgProducer, RocketConsumerResolveMapping resolveConsumer
            , RocketProperties rocketProperties) {
        return new DelayConsumerRegister(msgProducer, resolveConsumer, rocketProperties);
    }

}
