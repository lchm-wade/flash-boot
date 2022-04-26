package com.foco.boot.distributed.lock.redis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.foco.boot.distributed.lock.ILock;
import com.foco.boot.distributed.lock.LockCallBack;
import com.foco.context.common.RedisPrefix;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description todo
 * @Author lucoo
 * @Date 2021/5/7 11:50
 */
@Slf4j
public class RedisDistributedLock implements ILock {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisPrefix redisPrefix;

    @Override
    public <T> T tryLock(String lockKey, long waitTime, long leaseTime, LockCallBack<T> lockCallBack){
        if(StrUtil.isBlank(lockKey)){
            SystemException.throwException(FocoErrorCode.PARAMS_VALID.getCode(),"lockKey不能为空");
        }
        checkParam(waitTime,leaseTime,lockCallBack);
        lockKey = redisPrefix.getPrefix() + lockKey;
        RLock lock = redissonClient.getLock(lockKey);
        return doLock(lock, lockKey, waitTime, leaseTime, lockCallBack);
    }

    @Override
    public <T> T tryLock(String lockKey, LockCallBack<T> lockCallBack){
        return tryLock(lockKey, WAIT_TIME, LEASE_TIME, lockCallBack);
    }

    private <T> T doLock(RLock lock, Object lockKey, long waitTime, long leaseTime, LockCallBack<T> lockCallBack){
        Object object;
        boolean locked = false;
        log.info("开始获取分布式锁,key:{}",lockKey);
        try {
            locked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            SystemException.throwException(String.format("获取分布式锁异常lockKey:%s",lockKey), e);
        }
        if (!locked) {
            SystemException.throwException(String.format("等待%s毫秒后获取分布式锁失败lockKey:%s", waitTime, lockKey));
        }
        log.info("分布式锁获取成功,key:{}",lockKey);
        try {
            object=lockCallBack.apply();
        } catch (Throwable e) {
            throw e;
        } finally {
            if(lock instanceof RedissonMultiLock){
                lock.unlock();
            }else if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            log.debug("release lock [{}]", lockKey);
        }
        return (T) object;
    }

    @Override
    public <T> T tryMultiLock(List<String> lockKeys, LockCallBack<T> lockCallBack){
        return tryMultiLock(lockKeys, WAIT_TIME, LEASE_TIME, lockCallBack);
    }
    @Override
    public <T> T tryMultiLock(List<String> lockKeys, long waitTime, long leaseTime, LockCallBack<T> lockCallBack){
        if(CollectionUtil.isEmpty(lockKeys)){
            SystemException.throwException(FocoErrorCode.PARAMS_VALID.getCode(),"分布式锁MULTI模式下lockKeys不能为空");
        }
        checkParam(waitTime,leaseTime,lockCallBack);
        RLock[] rLocks = new RLock[lockKeys.size()];
        Collections.sort(lockKeys, String::compareTo);
        for (int i = 0, length = lockKeys.size(); i < length; i++) {
            String keys = lockKeys.get(i);
            keys = redisPrefix.getPrefix() + keys;
            lockKeys.set(i, keys);
            RLock lock = redissonClient.getLock(keys);
            rLocks[i] = lock;
        }
        RedissonMultiLock redissonMultiLock = new RedissonMultiLock(rLocks);
        return doLock(redissonMultiLock, lockKeys, waitTime, leaseTime, lockCallBack);
    }
    private void checkParam(long waitTime, long leaseTime, LockCallBack lockCallBack){
        if(lockCallBack==null){
            SystemException.throwException(FocoErrorCode.PARAMS_VALID.getCode(),"lockCallBack不能为空");
        }
        if(waitTime<=0L){
            SystemException.throwException(FocoErrorCode.PARAMS_VALID.getCode(),"waitTime不能为负数");
        }
        if(leaseTime==0L){
            SystemException.throwException(FocoErrorCode.PARAMS_VALID.getCode(),"leaseTime不能为0");
        }
    }
}
