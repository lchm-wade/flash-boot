package com.foco.boot.rabbit.converter;

import org.springframework.amqp.support.converter.DefaultClassMapper;

/**
 * json 转换映射
 * @Author lucoo
 * @Date 2021/6/27 10:13
 */
public class RabbitMqClassMapper extends DefaultClassMapper {

    /**
     * 构造函数初始化信任所有pakcage
     */
    public RabbitMqClassMapper() {
        super();
        //spring boot 2.0 中默认的信任包  只有  "java.util", "java.lang" （1.5 是* ，信任所有包）
        //如果不使用默认的消息转换器，需要重写DefaultClassMapper
        setTrustedPackages("*");
    }
}
