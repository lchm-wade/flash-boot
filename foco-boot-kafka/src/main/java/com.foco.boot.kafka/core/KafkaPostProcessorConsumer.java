package com.foco.boot.kafka.core;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.mq.constant.MqConstant;
import com.foco.mq.constant.MsgPropertyConstant;
import com.foco.mq.extend.ConsumeBeforeProcessorConsumer;
import com.foco.mq.model.Msg;

import java.lang.annotation.Annotation;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public class KafkaPostProcessorConsumer implements ConsumeBeforeProcessorConsumer {
    @Override
    public Msg postProcessBeforeConsumer(Msg msg, Annotation annotation) {
        if (annotation instanceof KafkaMsgConsumer) {
            KafkaMsgConsumer consumer = (KafkaMsgConsumer) annotation;
            String groupId=consumer.consumerGroup();
            //消费幂等唯一标识
            String idCard=groupId;
            msg.put(MsgPropertyConstant.ID_CARD, idCard);
            msg.put(MsgPropertyConstant.PACKAGE_TRANSACTION, String.valueOf(consumer.packageTransaction()));
            msg.put(MsgPropertyConstant.IDEMPOTENT, String.valueOf(consumer.idempotent()));
        }
        return msg;
    }

    @Override
    public int consumeOrder() {
        return MqConstant.IDEMPOTENT_BEFORE_PROCESSOR_WRAP_ORDER - 1;
    }
}
