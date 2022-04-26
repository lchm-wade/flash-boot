package com.foco.boot.rocket.core.consume.open;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.alibaba.fastjson.JSONObject;
import com.foco.boot.rocket.constant.MqConstant;
import com.foco.boot.rocket.core.consume.RocketConsumerResolveMapping;
import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.model.RocketMsg;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.context.asserts.Assert;
import com.foco.model.constant.FocoConstants;
import com.foco.model.exception.SystemException;
import com.foco.mq.MsgProducer;
import com.foco.mq.constant.MsgPropertyConstant;
import com.foco.mq.model.Msg;
import com.foco.mq.model.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ChenMing
 * @date 2021/11/5
 */
@Slf4j
public class DelayConsumerRegister implements ApplicationListener<ContextRefreshedEvent> {

    private final MsgProducer msgProducer;

    private final RocketConsumerResolveMapping resolveConsumer;

    private final RocketProperties rocketProperties;

    private final Set<String> addr = new ConcurrentHashSet<>();

    public DelayConsumerRegister(MsgProducer msgProducer, RocketConsumerResolveMapping resolveConsumer
            , RocketProperties rocketProperties) {
        this.msgProducer = msgProducer;
        this.resolveConsumer = resolveConsumer;
        this.rocketProperties = rocketProperties;
    }

    /**
     * 方法名、参数被{@link #registerDelayConsumer(RocketProperties)}引用
     * 修改时需要连同修改
     *
     * @param msg 延时消息信息
     */
    public void listener(Msg msg) {
        Msg sendMessage = new RocketMsg.Builder()
                .setBody(msg.getBody())
                .setTopic(msg.getProperties().get(MqConstant.WRAP_TOPIC))
                .setKeys(msg.getKeys())
                .build();
        Map<String, String> properties = msg.getProperties();
        properties.keySet().stream()
                .filter(k -> k.contains(FocoConstants.CONFIG_PREFIX))
                .forEach(k -> sendMessage.put(k, properties.get(k)));
        sendMessage.put(MsgPropertyConstant.DELAY_TIME, String.valueOf(0));
        sendMessage.getProperties().remove(MqConstant.DELAY_LEVEL);
        String surplusSecond = msg.getProperties().get(MqConstant.FOCO_ROCKET_DELAY_SURPLUS);
        if (!StringUtils.isEmpty(surplusSecond)) {
            long second = Long.parseLong(surplusSecond);
            if (second > 0) {
                String initialSecond = msg.getProperties().get(MsgPropertyConstant.INITIAL_DELAY_TIME);
                long initSecond = Long.parseLong(initialSecond);
                String creteTime = msg.getProperties().get(MsgPropertyConstant.CREATE_TIME);
                if (!StringUtils.isEmpty(creteTime)) {
                    long createL = Long.parseLong(creteTime);
                    long delayTime = (createL + initSecond * 1000 - System.currentTimeMillis()) / 1000;
                    if (delayTime > 0) {
                        sendMessage.put(MsgPropertyConstant.DELAY_TIME, String.valueOf(delayTime));
                    }
                }
            }
        }
        SendResult send = msgProducer.send(sendMessage);
        if (!send.isSucceed()) {
            String errorMsg = JSONObject.toJSONString(send.getResult());
            log.error("延时消息发送失败，消息信息：" + errorMsg);
            throw new SystemException("延时消息发送失败，消息信息：" + errorMsg);
        }
    }

    /**
     * 注册延时监听（满足开源版MQ也能做到任意时间延时）
     */
    public void registerDelayConsumer(RocketProperties rocketProperties) {
        if (!addr.contains(rocketProperties.getNamesrvAddr()) && rocketProperties.isUseOpen()) {
            Method method;
            String methodName = "listener";
            try {
                method = this.getClass().getMethod(methodName, Msg.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                throw new SystemException("RocketMq开源版解析延时监听找不到方法：" + methodName, e);
            }
            RocketConsumerProperty rocketConsumerProperty = new RocketConsumerProperty();
            rocketConsumerProperty.setTopic(MqConstant.DELAY_TOPIC);
            rocketConsumerProperty.setServerId(rocketProperties.getServerId());
            rocketConsumerProperty.setConsumerGroup(MqConstant.DELAY_GROUP + rocketProperties.getServerId().replace(".", "_"));
            rocketConsumerProperty.setIdempotent(false);
            boolean result = resolveConsumer.getRocketOpenConsumerResolve().resolveConsumer(this, method, rocketConsumerProperty);
            Assert.that(result).isTrue("RocketMq开源版解析延时监听失败！");
            addr.add(rocketProperties.getNamesrvAddr());
        }
    }

    /**
     * 根据传入的时间进行开源版消息级别推算
     * 计算出离入参second最近的级别下标
     *
     * @param second 时间
     * @return index
     */
    public static int covert(long second) {
        int[] arr = MqConstant.ROCKETMQ_DELAY_TIME;
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = (left + right) >> 1;
            if (arr[mid] == second || left + 1 == right) {
                return second > arr[right] ? right : mid;
            } else if (second > arr[mid]) {
                left = mid;
            } else if (second < arr[mid]) {
                right = mid;
            }
        }
        return left;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        registerDelayConsumer(rocketProperties);
        List<RocketProperties> multiServer = rocketProperties.getMultiServer();
        if (!CollectionUtils.isEmpty(multiServer)) {
            for (RocketProperties properties : multiServer) {
                registerDelayConsumer(properties);
            }
        }
    }
}
