package com.foco.boot.kafka.model;

import com.foco.mq.extend.Converter;
import com.foco.mq.model.Msg;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public class KafkaMsgConverter implements Converter<ProducerRecord> {
    @Override
    public Class<ProducerRecord> type() {
        return ProducerRecord.class;
    }

    /**
     * 将foco应用消息转为kafka-client-api发送消息
     *
     * @param msg foco消息
     * @return
     */
    @Override
    public ProducerRecord<String, Object> convert(Msg msg) {
        //发送主题
        String topic = msg.getTopic();
        //发送内容
        Object value = msg.getBody();
        //发送分区,如果有优先按分区路由
        //Integer partition = ObjectUtil.isEmpty(kafkaMsg.getPartition())?null: NumberUtils.parseNumber(kafkaMsg.getPartition(),Integer.class);
        //根据key(比如用户id,订单id)进行hash发送到相应的分区,可以保证同一个id相关的消息发送到同一个partition,从而保证partition级别的有序性
        String key = msg.getHashTarget();
        //kafka headers
        final List<Header> headers = new ArrayList<>();
        msg.getProperties().entrySet().forEach(entry -> {
            String keyItem = entry.getKey();
            String valueItem = entry.getValue();
            if (!StringUtils.isEmpty(keyItem) && !StringUtils.isEmpty(valueItem)) {
                headers.add(new RecordHeader(keyItem, valueItem.getBytes()));
            }
        });
        ProducerRecord<String, Object> message = new ProducerRecord<String,Object>(topic, null, null, key, value, headers);
        return message;
    }

}
