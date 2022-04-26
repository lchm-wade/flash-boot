package com.foco.boot.rocket.model;

import com.foco.boot.rocket.constant.MsgPropertyConstant;
import com.foco.boot.rocket.properties.RocketProperties;
import com.foco.mq.exception.MessagingException;
import com.foco.mq.extend.AbstractMqServerProperties;
import com.foco.mq.model.BaseProducerProperty;
import com.foco.mq.model.Msg;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

/**
 * @author ChenMing
 * @version 1.0.0
 * @description TODO
 * @date 2022/01/05 10:59
 * @since foco2.3.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RocketProducerProperty extends BaseProducerProperty {

    private String tag;

    @Override
    public void sonDecorate(Msg msg) {
        if (!StringUtils.isEmpty(tag)) {
            RocketMsg.setTag(msg, tag);
        }
    }

    @Override
    protected Class<? extends AbstractMqServerProperties> sonServerProperties() {
        return RocketProperties.class;
    }
}
