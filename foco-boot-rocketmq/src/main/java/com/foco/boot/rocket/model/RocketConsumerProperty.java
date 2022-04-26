package com.foco.boot.rocket.model;

import com.foco.boot.rocket.RocketConsumer;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.enums.ConsumeMode;
import com.foco.mq.model.BaseConsumerProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ChenMing
 * @version 1.0.0
 * @description TODO
 * @date 2021/12/24 14:10
 * @since foco2.3.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RocketConsumerProperty extends BaseConsumerProperty {

    /**
     * 消费者组
     */
    private String consumerGroup;

    /**
     * 支持","（英文）分隔监听多个topic
     */
    private String topic;

    /**
     * 监听同一个Topic的多个标签写法 "tag1 || tag2 || tag3"
     * 监听多个Topic支持","（英文）分隔监听多个订阅表达式 （此字段被","分割后与topic","分割后的下标一一映射）
     * <p>
     * subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br>
     * if null or * expression,meaning subscribe all
     */
    private String tag = "*";

    /**
     * 消费模式
     * @see ConsumeMode
     */
    private ConsumeMode consumeMode = ConsumeMode.CLUSTER;

    /**
     * TODO 没有使用seata的全局事务则不用关注
     * <p>
     * 是否包裹本地事务（部分业务使用seata情况下，不需要消费端被@Transactional包裹,
     * 否则会和seata的global事务冲突）
     */
    private boolean packageTransaction = true;

    /**
     * 策略名
     *
     * @return 策略名
     * @see RocketConsumerStrategy
     */
    private String strategy = RocketConsumerStrategy.IMMEDIATE;

    /**
     * 是否开启幂等
     *
     * @return true or false
     * @see RocketConsumerStrategy
     */
    private boolean idempotent = true;

    /**
     * 消息可能阻塞正在使用的线程的最长时间(以分钟为单位)。
     */
    private long consumeTimeout = 15;

    /**
     * 消费线程数量
     */
    private int consumeThread = 10;

    /**
     * 消费失败最大重试次数
     * <p>
     * 默认-1  （-1代表16次）
     */
    private int maxReconsumeTimes = -1;

    /**
     * 批量拉的最大消息量（虽有推模式，但是用拉模式实现的）
     * <p>该项指的是一次请求拉取的消息量
     * <p>
     * 默认32条  阈值1~1024
     */
    private int pullBatchSize = 32;

    /**
     * 从broker拉取的msgs({@link #pullBatchSize})的大小超过consumeMessageBatchMaxSize 的大小时，
     * 会对消息进行拆分，然后提交到线程池进行处理
     * 值越小，会更大化利用多线程处理（具体场景具体分析配置）
     * <p>
     * 默认1条  阈值1~1024
     */
    private int consumeMessageBatchMaxSize = 1;

    public void initialize(RocketConsumer consumer) {
        setConsumeMessageBatchMaxSize(consumer.consumeMessageBatchMaxSize());
        setConsumeMode(consumer.consumeMode());
        setConsumerGroup(consumer.consumerGroup());
        setConsumeThread(consumer.consumeThread());
        setConsumeTimeout(consumer.consumeTimeout());
        setIdempotent(consumer.idempotent());
        setMaxReconsumeTimes(consumer.maxReconsumeTimes());
        setPackageTransaction(consumer.packageTransaction());
        setPullBatchSize(consumer.pullBatchSize());
        setStrategy(consumer.strategy());
        setServerId(consumer.serverId());
        StringBuilder tagBuilder = new StringBuilder();
        for (String t : consumer.tag()) {
            tagBuilder.append(t).append(",");
        }
        setTag(tagBuilder.toString());
        StringBuilder topicBuilder = new StringBuilder();
        for (String t : consumer.topic()) {
            topicBuilder.append(t).append(",");
        }
        setTopic(topicBuilder.toString());
    }
}
