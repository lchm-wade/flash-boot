package com.foco.boot.rocket.model;

import com.foco.boot.rocket.constant.MsgPropertyConstant;
import com.foco.mq.model.Msg;

/**
 * @author ChenMing
 * @date 2021/10/20
 */
public class RocketMsg extends Msg {
    private RocketMsg() {
        super();
    }

    public String getTag() {
        return getProperties().get(MsgPropertyConstant.TAG);
    }

    public void setTag(String tag) {
        setTag(this, tag);
    }

    public static void setTag(Msg msg, String tag) {
        msg.put(MsgPropertyConstant.TAG, tag);
    }

    public static class Builder extends Msg.Builder {

        public Builder setTag(String tag) {
            putProperties(MsgPropertyConstant.TAG, tag);
            return this;
        }

        @Override
        public RocketMsg build() {
            RocketMsg msg = new RocketMsg();
            msg.setBody(body);
            msg.setTopic(topic);
            msg.setProperties(properties);
            return msg;
        }
    }
}
