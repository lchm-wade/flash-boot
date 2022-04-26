package com.foco.boot.web.executor.transmit;


import com.foco.context.core.LoginContextHolder;
import com.foco.context.executor.ThreadLocalTransmit;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/10 14:16
 **/
public class LoginContextTransmit implements ThreadLocalTransmit<String> {
    @Override
    public void set(String s) {
        LoginContextHolder.set(s);
    }

    @Override
    public String get() {
        return LoginContextHolder.get();
    }

    @Override
    public void remove() {
        LoginContextHolder.remove();
    }
}
