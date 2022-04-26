package com.foco.boot.db.properties;

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
@ConfigurationProperties(prefix = FieldEncryptProperties.PREFIX)
@Getter
@Setter
public class FieldEncryptProperties extends AbstractProperties {
    public static final String PREFIX= FocoConstants.CONFIG_PREFIX+"field.encrypt";
    public static FieldEncryptProperties getConfig(){
        return getConfig(FieldEncryptProperties.class);
    }
    private String fieldEncryptKey="qwesdf234QPJHECB123@*&sqPGfJr5x&";
}
