package com.foco.boot.mongo.properties;

import com.foco.model.constant.FocoConstants;
import com.foco.properties.AbstractProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ChenMing
 * @date 2021/10/9
 */
@ConfigurationProperties(MongoProperties.PREFIX)
@Setter
@Getter
public class MongoProperties extends AbstractProperties {
    public static final String PREFIX = FocoConstants.CONFIG_PREFIX + "mongo";

    private Boolean enabled = true;
}
