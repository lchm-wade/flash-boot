package com.foco.boot.distributed.lock;
@FunctionalInterface
public interface LockCallBack<T> {
     T apply();
}
