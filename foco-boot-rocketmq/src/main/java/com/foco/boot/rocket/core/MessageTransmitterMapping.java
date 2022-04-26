package com.foco.boot.rocket.core;

import com.foco.boot.rocket.core.consume.ons.RocketMessageOnsTransmitter;
import com.foco.boot.rocket.core.consume.open.DelayConsumerRegister;
import com.foco.boot.rocket.core.consume.open.RocketMessageTransmitter;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.mq.core.producer.FocoMsgProducer;
import com.foco.mq.extend.AbstractMessageTransmitter;
import com.foco.mq.extend.MessageTransmitter;
import com.foco.mq.model.Msg;
import com.foco.mq.model.SendResult;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 映射{@link RocketMessageTransmitter}与{@link RocketMessageOnsTransmitter}，交由spirng管理
 *
 * @author ChenMing
 * @version 1.0.0
 * @description TODO
 * @date 2022/01/12 16:51
 * @see RocketMessageTransmitter
 * @see RocketMessageOnsTransmitter
 * @since foco2.3.0
 */
public class MessageTransmitterMapping extends AbstractMessageTransmitter<RocketProperties> implements DisposableBean {

    private MessageTransmitter transmitter;

    @Resource
    private FocoMsgProducer focoMsgProducer;

    public MessageTransmitterMapping(RocketProperties serverProperties) {
        super(serverProperties);
        if (serverProperties.isUseOpen()) {
            transmitter = new RocketMessageTransmitter(serverProperties, focoMsgProducer);
        } else {
            transmitter = new RocketMessageOnsTransmitter(serverProperties, focoMsgProducer);
        }
    }

    @Override
    public List<Class<? extends Msg>> type() {
        return transmitter.type();
    }

    @Override
    public boolean sendT(Msg msg) {
        return transmitter.sendT(msg);
    }

    @Override
    public boolean sendT(Msg msg, long timeout) {
        return transmitter.sendT(msg, timeout);
    }

    @Override
    public SendResult send(Msg msg) {
        return transmitter.send(msg);
    }

    @Override
    public SendResult send(Msg msg, long timeout) {
        return transmitter.send(msg, timeout);
    }

    @Override
    public SendResult sendOrderly(Msg msg) {
        return transmitter.sendOrderly(msg);
    }

    @Override
    public SendResult sendOrderly(Msg msg, long timeout) {
        return transmitter.sendOrderly(msg, timeout);
    }

    @Override
    public SendResult sendBatch(LinkedList<Msg> msg) {
        return transmitter.sendBatch(msg);
    }

    @Override
    public void destroy() throws Exception {
        if (transmitter instanceof DisposableBean) {
            ((DisposableBean) transmitter).destroy();
        }
    }
}
