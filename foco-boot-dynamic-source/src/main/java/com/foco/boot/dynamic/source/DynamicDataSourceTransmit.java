package com.foco.boot.dynamic.source;

import com.foco.context.core.DataSourceContextHolder;
import com.foco.context.executor.ThreadLocalTransmit;

/**
 * @Description 线程池 多数据源传递
 * @Author lucoo
 * @Date 2021/6/4 14:30
 **/
public class DynamicDataSourceTransmit implements ThreadLocalTransmit<String> {

    @Override
    public void set(String o) {
        DataSourceContextHolder.setDataSource(o);
    }

    @Override
    public String get() {
        return DataSourceContextHolder.getDataSource();
    }

    @Override
    public void remove() {
        DataSourceContextHolder.clearDataSource();
    }
}
