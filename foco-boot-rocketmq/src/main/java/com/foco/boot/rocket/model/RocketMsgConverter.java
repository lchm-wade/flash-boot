package com.foco.boot.rocket.model;

import com.foco.boot.rocket.constant.MqConstant;
import com.foco.boot.rocket.constant.MsgPropertyConstant;
import com.foco.mq.extend.Converter;
import com.foco.mq.model.Msg;
import org.apache.rocketmq.common.message.Message;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author ChenMing
 * @date 2021/11/1
 */
public class RocketMsgConverter implements Converter<Message> {
    @Override
    public Class<Message> type() {
        return Message.class;
    }

    @Override
    public Message convert(Msg msg) {
        Message message = new Message();
        Map<String, String> properties = msg.getProperties();
        properties.keySet().forEach(key -> {
            String value = properties.get(key);
            if (!StringUtils.isEmpty(value)) {
                message.putUserProperty(key, properties.get(key));
            }
        });
        String delayLevel = msg.getProperties().get(MqConstant.DELAY_LEVEL);
        if (!StringUtils.isEmpty(delayLevel)) {
            message.setDelayTimeLevel(Integer.parseInt(delayLevel));
        }
        message.setTopic(msg.getTopic());
        message.setKeys(msg.getKeys());
        message.setTags(msg.getProperties().get(MsgPropertyConstant.TAG));
        message.setBody(msg.getBody());
        return message;
    }

}
