package com.foco.boot.kafka.core.consume;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.boot.kafka.model.KafkaMsg;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Slf4j
public class ConsumerWorker implements Callable<AtomicBoolean> {
    private List<ConsumerRecord<String,Object>> consumerRecords;
    private AbstractConsumerFunction abstractConsumerFunction;
    private KafkaMsgConsumer kafkaMsgConsumer;

    public ConsumerWorker(List<ConsumerRecord<String,Object>> consumerRecords, AbstractConsumerFunction abstractConsumerFunction, KafkaMsgConsumer kafkaMsgConsumer) {
        this.consumerRecords = consumerRecords;
        this.abstractConsumerFunction = abstractConsumerFunction;
        this.kafkaMsgConsumer = kafkaMsgConsumer;
    }
    @Override
    public AtomicBoolean call() throws Exception {
        AtomicBoolean isSuccess = new AtomicBoolean(true);
        consumerRecords.forEach(record -> {
            try {
                abstractConsumerFunction.consume(KafkaMsg.getMsg(record), kafkaMsgConsumer);
            } catch (Throwable e) {
                log.error("abstractConsumerFunction.consume exception:", e);
                isSuccess.set(false);
            }
        });
        return isSuccess;
    }
}
