package com.foco.boot.rocket.constant;

/**
 * @author ChenMing
 * @date 2021/11/1
 */
public interface RocketConsumerStrategy {
    /**
     * 推模式进行并发消费
     * 优点：能保证及时消费
     * 缺点：压力过大时，易造成服务器压力过高从而降低吞吐或无法提供服务
     */
    String IMMEDIATE = "immediate";

    /**
     * 顺序消费（一般用于顺序消息发送后的消费）
     *
     * @see RocketConsumerStrategy#IMMEDIATE 其它特性类似，消费能力会稍有下降
     */
    String IMMEDIATE_ORDERLY = "immediate_orderly";
}
