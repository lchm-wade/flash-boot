package com.foco.boot.db.bootstrap;


import com.foco.boot.db.autoconfigure.CustomInterceptorAutoConfiguration;
import com.foco.context.util.BootStrapPrinter;
import com.foco.model.constant.MainClassConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 14:13
 */
@Slf4j
@Import({
        CustomInterceptorAutoConfiguration.class,
})
@EnableTransactionManagement
@AutoConfigureBefore(name = MainClassConstant.MYBATIS_PLUS)
public class DbBootstrapConfiguration {
    @PostConstruct
    public void init() {
        BootStrapPrinter.log("foco-boot-db",this.getClass());
    }
}
