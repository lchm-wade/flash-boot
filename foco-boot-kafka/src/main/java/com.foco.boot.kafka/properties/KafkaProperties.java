package com.foco.boot.kafka.properties;

import com.foco.mq.constant.MqConstant;
import com.foco.properties.AbstractProperties;
import lombok.Data;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zachary
 * @version 1.0.0
 * @description TODO
 */
@ConfigurationProperties(KafkaProperties.KAFKA_PREFIX)
@Data
public class KafkaProperties extends AbstractProperties {

    public static KafkaProperties getConfig() {
        return getConfig(KafkaProperties.class);
    }

    public static final String KAFKA_PREFIX = MqConstant.CONFIG_PREFIX + "kafka";
    /**
     * Kafka群集信息列表，用于连接kafka集群,格式为 hostname1:port,hostname2:port,hostname3:port
     */
    private String bootstrap_servers;
    /**
     * 可靠性或性能保证
     * 0:producer不会等待broker发送ack(性能高可靠性低)
     * 1:当leader接收到消息后发送ack(默认值,介于性能和可靠性之间)
     * -1:当所有的follower都同步消息成功后发送ack(可靠性高性能低)
     */
    private String acks = "1";
    /**
     * 有序性保证
     * 默认值为5,设为1即可保证当前包未确认就不能发送下一个包从而实现有序性，但吞吐量会受到影响
     */
    private int max_in_flight_requests_per_connection = 5;

    /**
     * 幂等性保证：默认值false,当设置为true时,Producer将确保每个消息在Stream中只写入一个副本。
     * 如果为false，由于Broker故障导致Producer进行重试之类的情况可能会导致消息重复写入到Stream中
     */
    private Boolean enable_idempotence = false;
    /**
     * 可靠性保证
     * 客户端发送失败后重试的次数,默认值为0,若设置大于0的值,则客户端会将发送失败的记录重新发送。
     * 如果需要保证时序性需要设置max_in_flight_requests_per_connection为1
     */
    private int retries = 3;
    /**
     * 生产者发送数据时等待服务器返回响应的时间。默认值30000ms
     */
    private int request_timeout_ms = 30000;

    /*********************消费端配置***********************/
    /**
     * consumer提交方式设置。自动提交会先更新位移,再消费消息,如果消费程序出现故障，没消费完毕，可能丢失消息；
     * 手动提交先消费，再更新位移,如果更新位移失败,后续消息可能会重复消费
     */
    private Boolean enable_auto_commit = true;
    /**
     * consumer端一次拉取数据的最大条数
     */
    private int max_poll_records = 500;
}
