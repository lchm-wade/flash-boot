package com.foco.boot.rocket.core.consume.open;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.foco.boot.rocket.constant.MqConstant;
import com.foco.boot.rocket.core.MessageTransmitterMapping;
import com.foco.boot.rocket.model.RocketMsg;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.context.asserts.Assert;
import com.foco.model.exception.SystemException;
import com.foco.mq.core.producer.FocoMsgProducer;
import com.foco.mq.exception.MessagingException;
import com.foco.mq.extend.MessageTransmitter;
import com.foco.mq.model.Msg;
import com.foco.mq.model.SendResult;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.DisposableBean;

import java.util.*;

/**
 * @author ChenMing
 * @date 2021/10/19
 * @see MessageTransmitterMapping
 */
public class RocketMessageTransmitter implements MessageTransmitter, DisposableBean {

    private FocoMsgProducer focoMsgProducer;

    private DefaultMQProducer producer;

    private final RocketProperties rocketProperties;

    private MessageQueueSelector messageQueueSelector = new SelectMessageQueueByHash();

    private static final Set<String> PRODUCER_GROUP = new ConcurrentHashSet<>(8);

    public void setMessageQueueSelector(MessageQueueSelector messageQueueSelector) {
        this.messageQueueSelector = messageQueueSelector;
    }

    public RocketMessageTransmitter(RocketProperties rocketProperties, FocoMsgProducer focoMsgProducer) {
        this.focoMsgProducer = focoMsgProducer;
        this.rocketProperties = rocketProperties;
        String groupName;
        if (RocketProperties.class.getName().equals(rocketProperties.getGroupName())) {
            groupName = MqConstant.PRODUCER_GROUP;
        } else {
            groupName = rocketProperties.getGroupName();
        }
        if (PRODUCER_GROUP.contains(MqConstant.PRODUCER_GROUP)) {
            throw new SystemException("重复的生产组名字");
        }
        PRODUCER_GROUP.add(groupName);
        producer = new DefaultMQProducer(groupName);
        Assert.that(rocketProperties.getNamesrvAddr()).isNotEmpty(com.foco.mq.constant.MqConstant.NOT_EMPTY_CLUE.replace("{}", "Rocket NamesrvAddr"));
        producer.setNamesrvAddr(rocketProperties.getNamesrvAddr());
        try {
            producer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void pretreatmentParam(Msg msg) {
        pretreatmentDelay(msg);
    }

    public void pretreatmentDelay(Msg msg) {
        long delayTime = msg.getDelayTime();
        if (delayTime > 0) {
            int index = DelayConsumerRegister.covert(delayTime);
            int i = MqConstant.ROCKETMQ_DELAY_TIME[index];
            long surplus = delayTime - i;
            if (surplus >= 0) {
                msg.put(MqConstant.FOCO_ROCKET_DELAY_SURPLUS, String.valueOf(surplus));
            }
            msg.put(MqConstant.DELAY_LEVEL, String.valueOf(MqConstant.ROCKETMQ_DELAY_TIME_LEVEL[index]));
            msg.put(MqConstant.WRAP_TOPIC, msg.getTopic());
            msg.setTopic(MqConstant.DELAY_TOPIC);
        }
    }

    @Override
    public List<Class<? extends Msg>> type() {
        return Arrays.asList(Msg.class, RocketMsg.class);
    }

    @Override
    public boolean sendT(Msg msg) {
        return sendT(msg, rocketProperties.getSendTimeout());
    }

    @Override
    public boolean sendT(Msg msg, long timeout) {
        return focoMsgProducer.sendT(msg, timeout);
    }

    @Override
    public SendResult send(Msg msg) {
        return send(msg, producer.getSendMsgTimeout());
    }

    @Override
    public SendResult send(Msg msg, long timeout) {
        pretreatmentParam(msg);
        org.apache.rocketmq.client.producer.SendResult send;
        try {
            send = producer.send(msg.covert(Message.class), timeout);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
        return covert(send);
    }

    @Override
    public SendResult sendOrderly(Msg msg) {
        return sendOrderly(msg, rocketProperties.getSendTimeout());
    }

    @Override
    public SendResult sendOrderly(Msg msg, long timeout) {
        pretreatmentParam(msg);
        org.apache.rocketmq.client.producer.SendResult send;
        try {
            send = producer.send(msg.covert(Message.class), messageQueueSelector, msg.getHashTarget(), timeout);
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
        return covert(send);
    }

    @Override
    public SendResult sendBatch(LinkedList<Msg> msg) {
        throw new UnsupportedOperationException("暂未实现");
    }

    protected SendResult covert(org.apache.rocketmq.client.producer.SendResult sendResult) {
        SendResult result = new SendResult();
        if (sendResult != null && sendResult.getSendStatus() == SendStatus.SEND_OK) {
            result.setSucceed(true);
        }
        result.setResult(sendResult);
        return result;
    }


    @Override
    public void destroy() {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }
    }
}
