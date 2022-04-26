package com.foco.boot.dynamic.source;

import com.foco.context.core.DataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/4 14:30
 **/
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSource();
    }
}
