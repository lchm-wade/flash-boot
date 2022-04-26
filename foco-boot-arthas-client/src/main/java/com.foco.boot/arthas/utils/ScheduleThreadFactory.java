package com.foco.boot.arthas.utils;

import lombok.NonNull;

import java.util.concurrent.ThreadFactory;

/**
 * @author ChenMing
 */
public class ScheduleThreadFactory implements ThreadFactory {

    private final String name;

    public ScheduleThreadFactory(String name) {
        if (name == null || "".equals(name)) {
            throw new UnsupportedOperationException("name不能为空");
        }
        this.name = name;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r,
                name,
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
