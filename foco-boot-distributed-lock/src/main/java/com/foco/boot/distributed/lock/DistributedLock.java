package com.foco.boot.distributed.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基于Redisson的分布式锁注解
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    /**
     * 支持${}取值
     * @return
     */
    String lockKey() default "";

    /**
     * 获取锁等待时间(单位毫秒)，默认10秒
     */
    long waitTime() default ILock.WAIT_TIME;

    /**
     * 持锁时间(单位毫秒),超过这个时间，自动释放锁
     * 默认-1,会启动看门狗机制，业务没有执行完成之前会一直续期
     */
    long leaseTime() default ILock.LEASE_TIME;

    String[] lockKeys() default {};
    LockModel lockModel() default LockModel.SINGLE;
    enum LockModel{
        MULTI,SINGLE
    }
}
