package com.foco.boot.rocket.tools;

import com.aliyun.openservices.ons.api.Message;
import com.foco.boot.rocket.model.RocketMsg;
import com.foco.mq.model.Msg;

import java.util.Properties;

/**
 * @author ChenMing
 * @date 2021/11/11
 */
public class OnsMessageUtils {

    public static Msg getMsg(Message message) {
        Properties properties = message.getUserProperties();
        Msg msg = new RocketMsg.Builder().setBody(message.getBody())
                .setTopic(message.getTopic()).build();
        properties.keySet().forEach(key -> msg.put((String) key, properties.getProperty(String.valueOf(key))));
        return msg;
    }
}
