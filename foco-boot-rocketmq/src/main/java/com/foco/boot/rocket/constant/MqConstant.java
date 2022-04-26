package com.foco.boot.rocket.constant;

import com.foco.model.constant.FocoConstants;

/**
 * @author ChenMing
 * @date 2021/7/9
 */
public interface MqConstant {

    /**
     * 生产组
     */
    String PRODUCER_GROUP = "PRODUCER_GROUP";

    /**
     * 延时消息Topic
     */
    String DELAY_TOPIC = "FOCO_ROCKET_DELAY_TOPIC_NEW";

    /**
     * 延时消息监听GROUP
     */
    String DELAY_GROUP = "FOCO_ROCKET_DELAY_GROUP";

    /**
     * 延时消息Topic时，包装的topic
     */
    String WRAP_TOPIC = FocoConstants.CONFIG_PREFIX + "ROCKET_DELAY_TOPIC_WRAP";

    /**
     * 延时消息Topic时，包装的topic
     */
    String FOCO_ROCKET_DELAY_SURPLUS = FocoConstants.CONFIG_PREFIX + "ROCKET_DELAY_SURPLUS";

    String DELAY_LEVEL = FocoConstants.CONFIG_PREFIX + "DELAY_LEVEL";
    /**
     * RocketMQ延迟时间设置
     * 数组的值对应：1s、 5s、 10s、 30s、 1m、 2m、 3m、 4m、 5m、 6m、 7m、 8m、 9m、 10m、 20m、 30m、 1h、 2h
     */
    int[] ROCKETMQ_DELAY_TIME_LEVEL = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};

    int[] ROCKETMQ_DELAY_TIME = new int[]{1, 5, 10, 30, 60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 1200, 1800, 3600, 7200};

}
