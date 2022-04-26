package com.foco.boot.rocket;

import com.alibaba.fastjson.parser.Feature;
import com.foco.boot.rocket.constant.RocketConsumerStrategy;
import com.foco.boot.rocket.enums.ConsumeMode;
import com.foco.boot.rocket.properties.RocketProperties;

import java.lang.annotation.*;
import java.lang.reflect.Type;

/**
 * @author ChenMing
 * @date 2021/10/19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface RocketConsumer {
    /**
     * Topic
     * 支持${}进行注入
     * <p>可使用 {topic1,topic2} 进行多个topic的订阅，如果进行多个topic的订阅
     * 需要考虑接受数据的数据格式，可以统一用{@link com.foco.mq.model.Msg}接受
     * 随后自己调用{@link com.alibaba.fastjson.JSONObject#parseObject(byte[], Type, Feature...)}反序列化
     */
    String[] topic();

    /**
     * 消费组（唯一性标识）
     * 支持${}进行注入
     */
    String consumerGroup();

    /**
     * 服务id
     * 支持${}进行注入
     *
     * @return 返回""时会使用默认数据源{@link RocketProperties#getServerId()}
     */
    String serverId() default "";

    /**
     * 订阅表达式
     * 支持${}进行注入
     * <p>可使用 {tag1,tag2} 对多个{@link #topic()}关联，topic下标1关联tag下标1,topic2关联tag2以此类推，
     * 如果tag2没有设置，默认为{@code "*"}
     * <p>subscription expression.it only support or operation such as "tag1 || tag2 || tag3" <br>
     * if null or * expression,meaning subscribe all
     */
    String[] tag() default "*";

    /**
     * 消费模式
     */
    ConsumeMode consumeMode() default ConsumeMode.CLUSTER;

    /**
     * TODO 没有使用seata的全局事务则不用关注此项
     * <p>
     * 是否包裹本地事务（部分业务使用seata情况下，不需要消费端被@Transactional包裹,
     * 否则会和seata的global事务冲突）
     */
    boolean packageTransaction() default true;

    /**
     * 策略名
     *
     * @return 策略名
     * @see RocketConsumerStrategy
     */
    String strategy() default RocketConsumerStrategy.IMMEDIATE;

    /**
     * 是否开启幂等
     *
     * @return true or false
     * @see RocketConsumerStrategy
     */
    boolean idempotent() default true;

    /**
     * 消息可能阻塞正在使用的线程的最长时间(以分钟为单位)。
     */
    long consumeTimeout() default 15;

    /**
     * 消费线程数量
     */
    int consumeThread() default 10;

    /**
     * 消费失败最大重试次数
     *
     * @return 默认返回-1  （-1代表16次）
     */
    int maxReconsumeTimes() default -1;

    /**
     * 批量拉的最大消息量（虽然Rocket有推模式，但本质上底层依旧是拉模式的实现）
     * <p>该项指的是一次请求拉取的消息量
     *
     * @return 默认32条  阈值1~1024
     */
    int pullBatchSize() default 32;

    /**
     * 从broker拉取的msgs({@link #pullBatchSize()})的大小超过consumeMessageBatchMaxSize 的大小时，
     * 会对消息进行拆分，然后提交到线程池进行处理
     * 值越小，会更大化利用多线程处理（具体场景具体分析配置）
     *
     * @return 默认1条  阈值1~1024
     */
    int consumeMessageBatchMaxSize() default 1;
}
