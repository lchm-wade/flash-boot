package com.foco.boot.web.properties;

import com.foco.properties.AbstractProperties;
import com.foco.model.constant.FocoConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/2 14:43
 **/
@Getter
@Setter
@ConfigurationProperties(prefix = JacksonProperties.PREFIX)
public class JacksonProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"jackson";
    public static JacksonProperties getConfig(){
        return getConfig(JacksonProperties.class);
    }

    /**
     * 时间类型序列化
     * NORMAL: 2020-12-12 12:12:12这种格式
     * TIME_STAMP 10位 秒时间戳
     * TIME_STAMP_MS 13位 毫秒时间戳
     */
    private String dateTimePattern="yyyy-MM-dd HH:mm:ss";
    private String datePattern="yyyy-MM-dd";
    private String timePattern="HH:mm:ss";
}
