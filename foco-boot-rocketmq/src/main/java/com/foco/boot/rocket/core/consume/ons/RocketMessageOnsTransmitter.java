package com.foco.boot.rocket.core.consume.ons;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import com.foco.boot.rocket.constant.MqConstant;
import com.foco.boot.rocket.model.RocketMsg;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.context.asserts.Assert;
import com.foco.model.exception.SystemException;
import com.foco.mq.core.producer.FocoMsgProducer;
import com.foco.mq.exception.MessagingException;
import com.foco.mq.extend.MessageTransmitter;
import com.foco.mq.model.Msg;
import com.foco.mq.model.SendResult;
import org.springframework.beans.factory.DisposableBean;

import java.util.*;

import static com.foco.mq.constant.MqConstant.NOT_EMPTY_CLUE;

/**
 * @author ChenMing
 * @date 2021/11/8
 */
public class RocketMessageOnsTransmitter implements MessageTransmitter, DisposableBean {

    private final Producer producer;

    private final OrderProducer orderProducer;

    private final RocketProperties rocketProperties;

    private final FocoMsgProducer focoMsgProducer;

    private static final Set<String> PRODUCER_GROUP = new ConcurrentHashSet<>(8);

    public RocketMessageOnsTransmitter(RocketProperties rocketProperties, FocoMsgProducer focoMsgProducer) {
        valid(rocketProperties);
        this.rocketProperties = rocketProperties;
        this.focoMsgProducer = focoMsgProducer;
        Properties properties = new Properties();
        // AccessKey ID阿里云身份验证，在阿里云RAM控制台创建。
        properties.put(PropertyKeyConst.AccessKey, rocketProperties.getAccessKey());
        String groupName = getGroup(rocketProperties);
        properties.put(PropertyKeyConst.GROUP_ID, groupName);
        if (PRODUCER_GROUP.contains(groupName)) {
            throw new SystemException("重复的生产组名字：" + groupName);
        }
        PRODUCER_GROUP.add(groupName);
        // AccessKey Secret阿里云身份验证，在阿里云RAM控制台创建。
        properties.put(PropertyKeyConst.SecretKey, rocketProperties.getSecretKey());
        // 设置TCP接入域名，进入消息队列RocketMQ版控制台实例详情页面的接入点区域查看。
        properties.put(PropertyKeyConst.NAMESRV_ADDR, rocketProperties.getNamesrvAddr());
        properties.setProperty(PropertyKeyConst.SendMsgTimeoutMillis, String.valueOf(rocketProperties.getSendTimeout()));
        this.producer = ONSFactory.createProducer(properties);
        this.orderProducer = ONSFactory.createOrderProducer(properties);
        producer.start();
        orderProducer.start();
    }

    private void valid(RocketProperties rocketProperties) {
        Assert.that(rocketProperties.getNamesrvAddr()).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", "Rocket NamesrvAddr"));
        Assert.that(rocketProperties.getAccessKey()).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", "Rocket AccessKey"));
        Assert.that(rocketProperties.getSecretKey()).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", "Rocket SecretKey"));
    }

    private String getGroup(RocketProperties rocketProperties) {
        String groupName;
        if (RocketProperties.class.getName().equals(rocketProperties.getGroupName())) {
            groupName = MqConstant.PRODUCER_GROUP;
        } else {
            groupName = rocketProperties.getGroupName();
        }
        return groupName;
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
        return focoMsgProducer.sendT(msg);
    }

    @Override
    public SendResult send(Msg msg) {
        return send(msg, rocketProperties.getSendTimeout());
    }

    @Override
    public SendResult send(Msg msg, long timeout) {
        com.aliyun.openservices.ons.api.SendResult send;
        try {
            send = producer.send(msg.covert(Message.class));
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
        com.aliyun.openservices.ons.api.SendResult send;
        try {
            send = orderProducer.send(msg.covert(Message.class), msg.getHashTarget());
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
        return covert(send);
    }

    @Override
    public SendResult sendBatch(LinkedList<Msg> msg) {
        throw new UnsupportedOperationException("暂未实现");
    }

    protected SendResult covert(com.aliyun.openservices.ons.api.SendResult sendResult) {
        SendResult result = new SendResult();
        //商业版同步消息，只要不报错就是发送成功
        result.setSucceed(true);
        result.setResult(sendResult);
        return result;
    }

    @Override
    public void destroy() {
        if (Objects.nonNull(producer)) {
            producer.shutdown();
        }
        if (Objects.nonNull(orderProducer)) {
            orderProducer.shutdown();
        }
        PRODUCER_GROUP.remove(getGroup(rocketProperties));
    }
}
