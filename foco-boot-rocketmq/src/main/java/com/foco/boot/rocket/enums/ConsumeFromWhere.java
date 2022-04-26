package com.foco.boot.rocket.enums;

import com.foco.boot.rocket.RocketConsumer;

/**
 * @author ChenMing
 * @version 1.0.0
 * @description TODO
 * @date 2022/01/10 11:18
 * @since foco2.3.0
 */
public enum ConsumeFromWhere {
    /**
     * 一个新的订阅组第一次启动从队列的最后位置开始消费
     * 后续再启动接着上次消费的进度开始消费
     */
    CONSUME_FROM_LAST_OFFSET,
    /**
     *  一个新的订阅组第一次启动从队列的最前位置开始消费
     *  后续再启动接着上次消费的进度开始消费
     */
    CONSUME_FROM_FIRST_OFFSET,
    /**
     * 一个新的订阅组第一次启动从指定时间点开始消费
     * 后续再启动接着上次消费的进度开始消费
     * 时间点设置参见{@link RocketConsumer#consumeTimestamp()} (String)}
     */
    CONSUME_FROM_TIMESTAMP,
}
