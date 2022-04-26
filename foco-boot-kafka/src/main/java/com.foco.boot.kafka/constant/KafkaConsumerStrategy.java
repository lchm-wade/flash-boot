package com.foco.boot.kafka.constant;
/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
public interface KafkaConsumerStrategy {
    /**
     * 一个topic一个消费组一个KafkaConsumer线程,适用于顺序消费。
     * 可以通过集群来增加对于多个partition的并行消费能力,实现负载均衡,同一个消费组的每个消费者对于partition是互斥消费.
     */
    String ORDERLY = "orderly";
    /**
     * 一个topic一个消费组一个KafkaConsumer线程,消费线程将任务处理提交到线程池异步并行处理,适用于对顺序消费要求不高,消息量大的并发消费.
     * 可以通过集群来增加对于多个partition的并行消费能力。
     */
    String CONCURRENCY="concurrency";
}
