package com.foco.boot.distributed.lock;

import com.foco.context.util.DynamicParamParser;
import com.foco.model.constant.AopOrderConstants;
import com.foco.model.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * description----------
 *
 * @Author lucoo
 * @Date 2021/6/26 13:59
 */
@Aspect
@Slf4j
@Order(AopOrderConstants.REDIS_LOCK)
public class LockAspect {
    @Autowired
    private ILock lockService;
    /**
     * 切点(添加DistributedLock注解的方法)
     */
    @Pointcut("@annotation(com.foco.boot.distributed.lock.DistributedLock)")
    public void lockPoint() {
    }
    @Around("lockPoint()")
    public Object lockAround(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);
        final Throwable[] throwableSource = new Throwable[1];
        if(distributedLock.lockModel()== DistributedLock.LockModel.SINGLE){
            String lockKey = DynamicParamParser.handle(method,pjp.getArgs(), distributedLock.lockKey());
            Object o = lockService.tryLock(lockKey, distributedLock.waitTime(), distributedLock.leaseTime(), () -> {
                try {
                    return pjp.proceed(pjp.getArgs());
                } catch (Throwable throwable) {
                    throwableSource[0] = throwable;
                    log.error("分布式锁处理异常",throwable);
                }
                return null;
            });
            if(throwableSource[0]!=null){
                throw throwableSource[0];
            }
            return o;
        }else {
            if (distributedLock.lockModel() != DistributedLock.LockModel.MULTI) {
                SystemException.throwException("分布式锁暂不支持的模式");
            }
            List<String> composeKey=new ArrayList<>();
            for(String key:distributedLock.lockKeys()){
                composeKey.add(DynamicParamParser.handle(method,pjp.getArgs(),key));
            }
            Object o = lockService.tryMultiLock(composeKey, distributedLock.waitTime(), distributedLock.leaseTime(), () -> {
                try {
                    return pjp.proceed(pjp.getArgs());
                } catch (Throwable throwable) {
                    throwableSource[0] = throwable;
                    log.error("分布式锁处理异常",throwable);
                }
                return null;
            });
            if(throwableSource[0]!=null){
                throw throwableSource[0];
            }
            return o;
        }
    }
}
