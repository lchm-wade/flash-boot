package com.foco.boot.rocket.tools;

import com.foco.boot.rocket.model.RocketMsg;
import com.foco.mq.model.Msg;
import org.apache.rocketmq.common.message.Message;

import java.util.Map;

/**
 * @author ChenMing
 * @date 2021/11/11
 */
public class OpenMessageUtils {

    public static Msg getMsg(Message message) {
        Map<String, String> properties = message.getProperties();
        Msg msg = new RocketMsg.Builder().setBody(message.getBody())
                .setTopic(message.getTopic()).build();
        properties.keySet().forEach(key -> msg.put(key, properties.get(key)));
        return msg;
    }

}
