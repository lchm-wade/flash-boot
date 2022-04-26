package com.foco.boot.rocket.autoconfigure;

import com.aliyun.openservices.ons.api.Message;
import com.foco.boot.rocket.core.consume.ons.RocketConsumerResolve;
import com.foco.boot.rocket.core.consume.ons.strategy.ImmediateConsumer;
import com.foco.boot.rocket.core.consume.ons.strategy.ImmediateOrderlyConsumer;
import com.foco.boot.rocket.model.RocketOnsMsgConverter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商业版
 *
 * @author ChenMing
 * @date 2021/11/8
 */
@Configuration
@ConditionalOnClass(Message.class)
public class RocketOnsConfiguration {

    @Bean
    public ImmediateConsumer immediateOnsConsumer() {
        return new ImmediateConsumer();
    }

    @Bean
    public ImmediateOrderlyConsumer immediateOrderlyOnsConsumer() {
        return new ImmediateOrderlyConsumer();
    }

    @Bean
    public RocketOnsMsgConverter rocketOnsMsgConverter() {
        return new RocketOnsMsgConverter();
    }
}
