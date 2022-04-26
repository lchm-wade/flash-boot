package com.foco.boot.rocket.properties;

import com.foco.boot.rocket.model.RocketConsumerProperty;
import com.foco.boot.rocket.model.RocketProducerProperty;
import com.foco.context.asserts.Assert;
import com.foco.context.util.CollectionUtils;
import com.foco.context.util.StringUtils;
import com.foco.mq.constant.MqConstant;
import com.foco.mq.extend.AbstractMqServerProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

import static com.foco.mq.constant.MqConstant.NOT_EMPTY_CLUE;

/**
 * @author ChenMing
 * @date 2021/10/19
 */
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(RocketProperties.ROCKET_PREFIX)
@Data
public class RocketProperties extends AbstractMqServerProperties<RocketProperties
        , RocketProducerProperty
        , RocketConsumerProperty> {

    public static final String ROCKET_PREFIX = MqConstant.CONFIG_PREFIX + "rocket";

    private static final String CUE = "RocketMq配置";

    /**
     * {@link com.foco.boot.rocket.model.RocketMsg}生产者默认创建 true：开源 false：商用
     * TODO 字段含义修改：（原）是否使用开源版MQ
     */
    private boolean useOpen = true;

    /**
     * 命名服务地址
     */
    private String namesrvAddr;

    /**
     * 开源版：默认超时时间
     * 商用版：商用版的api没有单个消息的API超时时间，故此配置在商业版RocketMQ是全局
     */
    private long sendTimeout = 3000;

    /* *******************************
     * 下方属性为商业版需要关注
     * *******************************
     */

    /**
     * AccessKey ID阿里云身份验证，在阿里云RAM控制台创建。
     */
    private String accessKey;

    /**
     * AccessKey Secret阿里云身份验证，在阿里云RAM控制台创建。
     */
    private String secretKey;

    /**
     * 生产端的分组名，不填会默认为{@link #serverId}
     */
    private String groupName;

    @Override
    protected void sonInitialize() {
        if (StringUtils.isEmpty(getGroupName())) {
            setGroupName(getServerId());
        }
        if (!CollectionUtils.isEmpty(getMultiServer())) {
            Set<String> groups = new HashSet<>();
            groups.add(getGroupName());
            for (RocketProperties rocketProperties : getMultiServer()) {
                if (StringUtils.isEmpty(rocketProperties.getGroupName())) {
                    rocketProperties.setGroupName(rocketProperties.getServerId());
                }
                Assert.that(groups.contains(rocketProperties.getGroupName())).isFalse(CUE + "存在重复的groupName：" + rocketProperties.getGroupName());
            }
        }
    }

    @Override
    protected void initializeServer(RocketProperties properties) {
        if (properties.isUseOpen()) {
            properties.validOpen();
        } else {
            properties.validOns();
        }
    }

    @Override
    protected void initializeConsumer(RocketConsumerProperty property) {
        String cue = NOT_EMPTY_CLUE.replace("{}", CUE + "消费者consumerId：" + property.getConsumerId() + "的{}");
        if (!StringUtils.isEmpty(property.getPropertyId())) {
            RocketConsumerProperty consumerOptions = getConsumerOptions(property.getPropertyId());
            Assert.that(consumerOptions).isNotNull("不存在的Consumer propertyId：" + property.getPropertyId());
            if (StringUtils.isEmpty(property.getTopic())) {
                property.setTopic(consumerOptions.getTopic());
            }
            if (StringUtils.isEmpty(property.getConsumerGroup())) {
                property.setConsumerGroup(consumerOptions.getConsumerGroup());
            }
        }
        Assert.that(property.getTopic()).isNotEmpty(cue.replace("{}", "topic"));
        Assert.that(property.getConsumerGroup()).isNotEmpty(cue.replace("{}", "consumerGroup"));
    }

    @Override
    protected void initializeProducer(RocketProducerProperty property) {
        String cue = NOT_EMPTY_CLUE.replace("{}", CUE + "生产者producerId：" + property.getProducerId() + "的{}");
        if (!StringUtils.isEmpty(property.getPropertyId())) {
            RocketProducerProperty producerOptions = getProducerOptions(property.getPropertyId());
            Assert.that(producerOptions).isNotNull("不存在的Producer propertyId：" + property.getPropertyId());
            if (StringUtils.isEmpty(property.getTopic())) {
                property.setTopic(producerOptions.getTopic());
            }
        }
        Assert.that(property.getTopic()).isNotEmpty(cue.replace("{}", "topic"));
    }

    public void validOpen() {
        Assert.that(namesrvAddr).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", CUE + "serverId：" + serverId + "，namesrvAddr"));
    }

    public void validOns() {
        validOpen();
        Assert.that(accessKey).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", CUE + "serverId：" + serverId + "，accessKey"));
        Assert.that(secretKey).isNotEmpty(NOT_EMPTY_CLUE.replace("{}", CUE + "serverId：" + serverId + "，secretKey"));
    }
}
