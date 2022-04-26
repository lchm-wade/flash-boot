package com.foco.boot.redis.serializer;


import cn.hutool.core.util.StrUtil;
import com.foco.context.common.RedisPrefix;
import com.foco.context.core.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 自定义key序列化方式
 *
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
@Slf4j
public class KeyStringSerializer implements RedisSerializer<String> {

    private final Charset charset;

    public KeyStringSerializer() {
        this.charset = StandardCharsets.UTF_8;
    }
    @Override
    public byte[] serialize(String originalValue) throws SerializationException {
        String newValue =  getKeyPrefix()+originalValue;
        return  newValue.getBytes ( charset );
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        String saveKey = new String ( bytes, charset );
        String keyPrefix= getKeyPrefix();
        if(StrUtil.isNotBlank(keyPrefix)){
            int indexOf = saveKey.indexOf ( keyPrefix );
            if (indexOf > 0) {
                log.info ( "key缺少前缀" );
            } else {
                saveKey = saveKey.substring ( indexOf );
            }
            log.info ( "saveKey:{}",saveKey);
        }
        return saveKey;
    }
    private String getKeyPrefix(){
        return SpringContextHolder.getBean(RedisPrefix.class).getPrefix();
    }
}
