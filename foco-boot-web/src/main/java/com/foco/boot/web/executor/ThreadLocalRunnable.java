package com.foco.boot.web.executor;


import com.foco.context.core.FocoContextManager;
import com.foco.context.core.ThreadLocalTransmitManager;
import com.foco.context.executor.ThreadLocalManager;
import lombok.extern.slf4j.Slf4j;
/**
 * description： 解决ThreadLocal 跨线程传递
 * @Author lucoo
 * @Date 2021/6/2 9:57
 */
@Slf4j
public class ThreadLocalRunnable implements Runnable {
    private Runnable runnable;
    /**
     * ContextThreadLocal对象
     */
    private ThreadLocalTransmitManager threadLocalTransmitManager;
    public ThreadLocalRunnable(Runnable runnable) {
        this.runnable = runnable;
        this.threadLocalTransmitManager=new ThreadLocalTransmitManager();
    }
    @Override
    public void run() {
        threadLocalTransmitManager.set();
        try {
            runnable.run();
        } catch (Throwable e){
            log.error("线程执行异常",e);
        }finally {
            threadLocalTransmitManager.remove();
        }
    }
}
