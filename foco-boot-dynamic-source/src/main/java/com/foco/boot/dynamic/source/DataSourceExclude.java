package com.foco.boot.dynamic.source;

import cn.hutool.core.util.StrUtil;
import com.foco.model.constant.FocoConstants;
import com.foco.model.constant.MainClassConstant;
import com.foco.model.spi.ExcludeAutoConfigure;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

/**
 * @author lucoo
 * @version 1.0.0
 * @description TODO
 * @date 2021/07/30 11:45
 */
public class DataSourceExclude implements ExcludeAutoConfigure {
    @Override
    public void exclude(List<String> excludeList,Object environment) {
       excludeList.add(MainClassConstant.DRUID_DATA_SOURCE_AUTO_CONFIGURATION_CLASS);
    }
}
