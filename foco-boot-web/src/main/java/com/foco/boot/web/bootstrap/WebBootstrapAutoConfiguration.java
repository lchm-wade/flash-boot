package com.foco.boot.web.bootstrap;


import com.foco.boot.web.autoconfigure.*;
import com.foco.context.util.BootStrapPrinter;
import com.foco.model.constant.FocoConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/23 17:14
 **/
@ComponentScan(basePackages = {FocoConstants.MAIN_PACKAGE_WEB})
@Slf4j
@Import({
        JacksonAutoConfiguration.class,
        CustomWebMvcConfigurer.class,
        ThreadPoolConfiguration.class,
        CorsAutoConfiguration.class,
        CommonAutoConfiguration.class,
        ParamLogAutoConfiguration.class,
        WebAsyncConfigurer.class
})
public class WebBootstrapAutoConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-web",this.getClass());
    }
}
