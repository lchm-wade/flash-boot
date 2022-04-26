package com.foco.boot.distributed.lock;

import java.util.List;

public interface ILock {
    long WAIT_TIME=10000L;
    long LEASE_TIME=-1L;
    <T> T tryLock(String lockKey, long waitTime, long leaseTime, LockCallBack<T> lockCallBack);
    <T> T tryLock(String lockKey, LockCallBack<T> lockCallBack);
    <T> T tryMultiLock(List<String> lockKeys, LockCallBack<T> lockCallBack);
    <T> T tryMultiLock(List<String> lockKeys, long waitTime, long leaseTime, LockCallBack<T> lockCallBack);
}
