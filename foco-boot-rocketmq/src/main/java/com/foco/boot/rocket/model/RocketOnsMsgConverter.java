package com.foco.boot.rocket.model;

import com.aliyun.openservices.ons.api.Message;
import com.foco.boot.rocket.constant.MsgPropertyConstant;
import com.foco.mq.extend.Converter;
import com.foco.mq.model.Msg;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author ChenMing
 * @date 2021/11/8
 */
public class RocketOnsMsgConverter implements Converter<Message> {

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
                message.putUserProperties(key, properties.get(key));
            }
        });
        String delayTime = msg.getProperties().get(com.foco.mq.constant.MsgPropertyConstant.DELAY_TIME);
        if (!StringUtils.isEmpty(delayTime)) {
            message.setStartDeliverTime(System.currentTimeMillis() + Long.valueOf(delayTime) * 1000);
        }
        message.setTopic(msg.getTopic());
        message.setKey(msg.getKeys());
        message.setTag(msg.getProperties().get(MsgPropertyConstant.TAG));
        message.setBody(msg.getBody());
        return message;
    }
}
