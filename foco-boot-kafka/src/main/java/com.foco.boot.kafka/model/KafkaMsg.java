package com.foco.boot.kafka.model;

import com.foco.boot.kafka.constant.Constant;
import com.foco.mq.model.Msg;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public class KafkaMsg extends Msg {
    /**
     * 将kafka消息转换为foco消息
     *
     * @param message
     * @return
     */
    public static Msg getMsg(ConsumerRecord<String,Object> message) {
        String topic = message.topic();
        Object value = message.value();
        Headers headers = message.headers();
        //将响应headers设置回msg
        Map<String,String> headersMap = new HashMap<>();
        if(headers!=null){
            headers.iterator().forEachRemaining(header -> {
                headersMap.put(header.key(),new String(header.value()));
            });
        }
        headersMap.put(Constant.PARTITION, String.valueOf(message.partition()));
        headersMap.put(Constant.OFFSET, String.valueOf(message.offset()));
        Msg msg;
        if (value instanceof byte[]) {
            msg = new KafkaMsg.Builder()
                    .setProperties(headersMap)
                    .setBody((byte[]) value)
                    .setTopic(topic)
                    .setHashTarget(message.key() == null ? null : message.key())
                    .build();
        } else {
            msg = new KafkaMsg.Builder()
                    .setProperties(headersMap)
                    .setBody(value)
                    .setTopic(topic)
                    .setHashTarget(message.key() == null ? null : message.key())
                    .build();
        }
        return msg;
    }

    public static class Builder extends Msg.Builder {
        @Override
        public KafkaMsg build() {
            KafkaMsg msg = new KafkaMsg();
            msg.setBody(body);
            msg.setTopic(topic);
            msg.setProperties(properties);
            return msg;
        }
    }
}
