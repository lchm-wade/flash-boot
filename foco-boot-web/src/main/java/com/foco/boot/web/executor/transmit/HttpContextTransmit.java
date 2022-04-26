package com.foco.boot.web.executor.transmit;


import com.foco.context.executor.ThreadLocalTransmit;
import com.foco.context.util.HttpContext;
import org.springframework.web.context.request.RequestAttributes;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/10 14:18
 **/
public class HttpContextTransmit implements ThreadLocalTransmit<RequestAttributes> {
    @Override
    public void set(RequestAttributes requestAttributes) {
        HttpContext.setRequestAttributes(requestAttributes);
    }
    @Override
    public RequestAttributes get() {
        return HttpContext.getRequestAttributes();
    }

    @Override
    public void remove() {
        HttpContext.removeRequestAttributes();
    }
}
