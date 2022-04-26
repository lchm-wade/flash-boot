package com.foco.boot.web.autoconfigure;

import com.foco.boot.web.executor.transmit.HttpContextTransmit;
import com.foco.boot.web.executor.transmit.LoginContextTransmit;
import com.foco.boot.web.support.pulish.EventPublish;
import org.springframework.context.annotation.Bean;
/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/5/13 9:04
 **/
public class CommonAutoConfiguration {
    @Bean
    EventPublish eventPublish(){
        return new EventPublish();
    }
    @Bean
    HttpContextTransmit httpContextTransmit(){
        return new HttpContextTransmit();
    }
    @Bean
    LoginContextTransmit loginContextTransmit(){
        return new LoginContextTransmit();
    }
}
