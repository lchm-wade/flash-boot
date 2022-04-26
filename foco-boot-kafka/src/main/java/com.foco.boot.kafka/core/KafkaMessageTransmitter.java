package com.foco.boot.kafka.core;

import com.foco.boot.kafka.constant.Constant;
import com.foco.boot.kafka.model.KafkaMsg;
import com.foco.boot.kafka.properties.KafkaProperties;
import com.foco.mq.core.producer.FocoMsgProducer;
import com.foco.mq.exception.MessagingException;
import com.foco.mq.extend.MessageTransmitter;
import com.foco.mq.model.Msg;
import com.foco.mq.model.SendResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Future;
/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public class KafkaMessageTransmitter implements MessageTransmitter {

    @Resource
    private FocoMsgProducer focoMsgProducer;

    private KafkaProducer<String, Object> producer;

    private KafkaProperties kafkaProperties;

    private KafkaProducer<String, Object> initKafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrap_servers());
        boolean enable_idempotence = kafkaProperties.getEnable_idempotence();
        if(!enable_idempotence){
            props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getAcks());
            props.put(ProducerConfig.RETRIES_CONFIG,kafkaProperties.getRetries());
            props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,kafkaProperties.getMax_in_flight_requests_per_connection());
        }else{
            props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,true);
        }

        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaProperties.getRequest_timeout_ms());
        /**
         * 对ProducerRecord中的key进行序列化
         */
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        /**
         * 对ProducerRecord中的value进行序列化
         */
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return new KafkaProducer<String,Object>(props);
    }

    public KafkaMessageTransmitter(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        producer = initKafkaProducer();
    }

    @Override
    public List<Class<? extends Msg>> type() {
        return Arrays.asList(KafkaMsg.class);
    }

    @Override
    public boolean sendT(Msg msg) {
        return focoMsgProducer.sendT(msg);
    }

    @Override
    public boolean sendT(Msg msg, long timeout) {
        return sendT(msg);
    }

    /**
     * 同步发送消息
     *
     * @param msg 消息
     * @return
     */
    @Override
    public SendResult send(Msg msg) {
        RecordMetadata recordMetadata;
        try {
            ProducerRecord producerRecord = msg.covert(ProducerRecord.class);
            Future<RecordMetadata> future = producer.send(producerRecord);
            recordMetadata = future.get();
        } catch (Exception e) {
            throw new MessagingException(e.getMessage(), e);
        }
        return covert(recordMetadata);
    }

    @Override
    public SendResult send(Msg msg, long timeout) {
        return send(msg);
    }

    /**
     * 同步发送顺序消息
     *
     * @param msg 消息 设置setHashTarget（String）来进行分区的分发，
     *            如果需要发往同一个分区，需要设成一样.根据hashTarget(比如用户id,订单id)进行hash发送到相应的分区,可以保证同一个id相关的消息发送到同一个partition,从而保证partition级别的有序性
     * @return
     */
    @Override
    public SendResult sendOrderly(Msg msg) {
        return send(msg);
    }

    @Override
    public SendResult sendOrderly(Msg msg, long timeout) {
        return sendOrderly(msg);
    }

    @Override
    public SendResult sendBatch(LinkedList<Msg> msg) {
        throw new UnsupportedOperationException("暂未实现");
    }

    protected SendResult covert(RecordMetadata recordMetadata) {
        SendResult result = new SendResult();
        if (recordMetadata != null) {
            result.setSucceed(true);
            long offset = recordMetadata.offset();
            int partition= recordMetadata.partition();
            Map<String,String> record = new HashMap<>();
            record.put(Constant.TOPIC,recordMetadata.topic());
            record.put(Constant.PARTITION,String.valueOf(partition));
            record.put(Constant.OFFSET,String.valueOf(offset));
            result.setResult(record);
        }
        return result;
    }


}
