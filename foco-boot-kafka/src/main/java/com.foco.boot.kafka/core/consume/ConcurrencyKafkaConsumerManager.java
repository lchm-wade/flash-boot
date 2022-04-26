package com.foco.boot.kafka.core.consume;

import com.foco.boot.kafka.KafkaMsgConsumer;
import com.foco.mq.core.consumer.AbstractConsumerFunction;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个topic一个消费组一个KafkaConsumer线程(对于一个topic,同消费组消费者负载均衡,不同消费组消费者发布订阅)
 *
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Slf4j
public class ConcurrencyKafkaConsumerManager extends KafkaConsumerManager {

    private ExecutorService executor;
    //private int corePoolSize;
    private int concurrency;

    public ConcurrencyKafkaConsumerManager(KafkaConsumer<String,Object> kafkaConsumer, AbstractConsumerFunction abstractConsumerFunction, KafkaMsgConsumer kafkaMsgConsumer, boolean autoCommit) {
        super(kafkaConsumer, abstractConsumerFunction, kafkaMsgConsumer, autoCommit);
        concurrency = kafkaMsgConsumer.concurrency();
        int maxConcurrency = Runtime.getRuntime().availableProcessors() * 2;
        if (concurrency > maxConcurrency) {
            concurrency = maxConcurrency;
        } else if (concurrency < 1) {
            concurrency = 1;
        }
        //corePoolSize = concurrency / 2 <= 0 ? 1 : concurrency / 2;
        executor = new ThreadPoolExecutor(concurrency, concurrency, 3, TimeUnit.MINUTES, new LinkedBlockingQueue<>(256), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void run() {
        addShutdownHook();
        try {
            kafkaConsumer.subscribe(Collections.singletonList(kafkaMsgConsumer.topic()));
            while (flag) {
                //允许消费者请求在长轮询中阻塞，等待数据到达
                ConsumerRecords<String, Object> records = kafkaConsumer.poll(Duration.ofMillis(300));
                if (records.isEmpty()) {
                    continue;
                }
                List<Future<AtomicBoolean>> futureList = new ArrayList<>();
                //分成concurrency等分
                int batch=records.count()/concurrency;
                int batchSize = batch==0?1:batch;
                List<ConsumerRecord<String,Object>> consumerRecordList =new ArrayList<>();
                records.partitions().forEach(partition->{
                    List<ConsumerRecord<String,Object>> consumerRecordListItem=records.records(partition);
                    consumerRecordList.addAll(consumerRecordListItem);
                });
                List<List<ConsumerRecord<String,Object>>> partition = Lists.partition(consumerRecordList,batchSize);
                for (List<ConsumerRecord<String,Object>> partitionItem:partition) {
                    ConsumerWorker consumerWorker = new ConsumerWorker(partitionItem, abstractConsumerFunction, kafkaMsgConsumer);
                    Future<AtomicBoolean> future = executor.submit(consumerWorker);
                    futureList.add(future);
                }
                AtomicBoolean isSuccess = new AtomicBoolean(true);
                futureList.forEach(future -> {
                    try {
                        AtomicBoolean atomicBoolean = future.get();
                        if(!atomicBoolean.get()){
                            isSuccess.set(false);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("An exception occurred while obtaining asynchronous execution results.",e);
                        isSuccess.set(false);
                    }
                });
                if (!autoCommit && isSuccess.get()) {
                    kafkaConsumer.commitSync();
                }
            }
        } catch (Exception e) {//无法处理的异常,消费线程退出
            log.error("consume kafka msg failed.", e);
        } finally {
            kafkaConsumer.close();
            if (executor != null) {
                log.info("shutdown executor!");
                executor.shutdown();
            }
        }
    }
}
