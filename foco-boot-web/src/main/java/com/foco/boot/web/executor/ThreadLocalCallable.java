package com.foco.boot.web.executor;


import com.foco.context.core.FocoContextManager;
import com.foco.context.core.ThreadLocalTransmitManager;
import com.foco.context.executor.ThreadLocalManager;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;

/**
 * description： 解决ThreadLocal 跨线程传递
 * @Author lucoo
 * @Date 2021/6/2 9:57
 */
@Slf4j
public class ThreadLocalCallable<V> implements Callable<V> {
    private Callable<V> callable;
    /**
     * ContextThreadLocal对象
     */
    private ThreadLocalTransmitManager threadLocalTransmitManager;

    public ThreadLocalCallable(Callable<V> callable) {
        this.callable = callable;
        this.threadLocalTransmitManager=new ThreadLocalTransmitManager();
    }
    @Override
    public V call() throws Exception {
        threadLocalTransmitManager.set();
        V result = null;
        try {
            result = callable.call();
        }catch (Throwable e){
            log.error("线程执行异常",e);
        } finally {
            threadLocalTransmitManager.remove();
        }
        return result;
    }
}
