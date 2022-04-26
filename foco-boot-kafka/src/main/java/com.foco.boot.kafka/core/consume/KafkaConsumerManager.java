package com.foco.boot.kafka.core.consume;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.boot.kafka.model.KafkaMsg;
import com.foco.model.exception.SystemException;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个topic一个消费组一个KafkaConsumer线程(对于一个topic,同消费组消费者负载均衡,不同消费组消费者发布订阅)
 *
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Slf4j
public class KafkaConsumerManager implements Runnable {
    protected KafkaConsumer<String, Object> kafkaConsumer;
    protected AbstractConsumerFunction abstractConsumerFunction;
    protected KafkaMsgConsumer kafkaMsgConsumer;
    protected volatile boolean flag = true;
    protected boolean autoCommit = true;

    public KafkaConsumerManager(KafkaConsumer<String, Object> kafkaConsumer, AbstractConsumerFunction abstractConsumerFunction, KafkaMsgConsumer kafkaMsgConsumer, boolean autoCommit) {
        this.kafkaConsumer = kafkaConsumer;
        this.abstractConsumerFunction = abstractConsumerFunction;
        this.kafkaMsgConsumer = kafkaMsgConsumer;
        this.autoCommit = autoCommit;
    }

    @Override
    public void run() {
        addShutdownHook();
        try {
            kafkaConsumer.subscribe(Collections.singletonList(kafkaMsgConsumer.topic()));
            while (flag) {
                //1、拉取消息,允许消费者请求在长轮询中阻塞，等待数据到达
                ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(300));
                //2、处理消息
                if (records == null || records.isEmpty()) {
                    continue;
                }
                Set<TopicPartition> partitions = records.partitions();
                AtomicBoolean isSuccess = new AtomicBoolean(true);
                for (TopicPartition partition : partitions) {
                    //每个分区的数据串行单独处理
                    List<ConsumerRecord<String, Object>> consumerRecords = records.records(partition);
                    for (Iterator<ConsumerRecord<String, Object>> it = consumerRecords.iterator(); it.hasNext(); ) {
                        ConsumerRecord<String, Object> recordItem = null;
                        try {
                            recordItem = it.next();
                            abstractConsumerFunction.consume(KafkaMsg.getMsg(recordItem), kafkaMsgConsumer);
                        } catch (Exception e) {
                            log.error("abstractConsumerFunction.consume exception:", e);
                            isSuccess.set(false);
                           /* if(!autoCommit){
                                //单独提交partition的offset
                                long offset = recordItem.offset();
                                Map<TopicPartition, OffsetAndMetadata> partitionOffsets = Collections.singletonMap(partition,new OffsetAndMetadata(offset));
                                kafkaConsumer.commitSync(partitionOffsets);
                            }
                            break;*/
                        }
                    }
                }
                //3、处理成功,如果是手动提交则提交
                if (!autoCommit && isSuccess.get()) {
                    kafkaConsumer.commitSync();
                }
            }
        } catch (Exception e) {//无法处理的异常,消费线程退出
            log.error("consume kafka msg failed.", e);
        } finally {
            kafkaConsumer.close();
        }
    }

    protected void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("KafkaConsumerManager shutdown!");
                shutdown();
            }
        });
    }

    protected void shutdown() {
        flag = false;
    }
}
