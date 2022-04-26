package com.foco.boot.web.executor;


import com.foco.context.executor.ThreadLocalExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * description：   解决ThreadLocal 跨线程传递
 *
 * @Author lucoo
 * @Date 2021/6/2 9:57
 */
@Slf4j
public class BootThreadLocalExecutor implements ThreadLocalExecutor {
    private ThreadPoolTaskExecutor delegate;

    public BootThreadLocalExecutor(ThreadPoolTaskExecutor delegate) {
        this.delegate=delegate;
    }
    @Override
    public void execute(Runnable command) {
        delegate.execute(new ThreadLocalRunnable(command));
    }

    @Override
    public Future<?> submit(Runnable command) {
        return delegate.submit(new ThreadLocalRunnable(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return delegate.submit(new ThreadLocalCallable<>(callable));
    }
}
