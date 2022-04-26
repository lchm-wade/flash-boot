package com.foco.boot.kafka;

import com.foco.boot.kafka.constant.KafkaConsumerStrategy;

import java.lang.annotation.*;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface KafkaMsgConsumer {

    /**
     * Topic
     * 支持${}进行注入
     */
    String topic();

    /**
     * 消费组:即group.id的配置
     * 支持${}进行注入
     */
    String consumerGroup();

    /**
     * TODO 没有使用seata的全局事务则不用关注
     *
     * 是否包裹本地事务（部分业务使用seata情况下，不需要消费端被@Transactional包裹,
     * 否则会和seata的global事务冲突）
     */
    boolean packageTransaction() default false;

    /**
     * 消费者线程数,总的消费者线程数一般小于等于一个topic的分区数
     * @return
     */
    int consumerNum() default 1;
    /**
     * 消费者并发处理线程数
     * @return
     */
    int concurrency() default 1;

    /**
     * 策略名
     *
     * @return 策略名
     */
    String strategy() default KafkaConsumerStrategy.CONCURRENCY;

    /**
     * 是否开启幂等
     *
     * @return true or false
     */
    boolean idempotent() default false;

}
