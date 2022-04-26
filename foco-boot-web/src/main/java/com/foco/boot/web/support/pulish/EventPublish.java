package com.foco.boot.web.support.pulish;

import cn.hutool.core.collection.CollectionUtil;
import com.foco.context.executor.ThreadLocalExecutor;
import com.foco.context.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部事件发布订阅机制
 *
 * @Author lucoo
 * @Date 2021/6/23 18:16
 */
@Slf4j
public class EventPublish<T> implements ApplicationContextAware {
    @Autowired
    private ThreadLocalExecutor executor;
    private Map<String, List<EventListener>> changeListenerMap = new HashMap<>();

    public void publishEvent(T t, boolean async) {
        StackTraceElement caller=Thread.currentThread().getStackTrace()[2];
        log.info(String.format("事件源:{%s}发布:{%s}事件", caller.getClassName()+"."+caller.getMethodName(),t.getClass().getSimpleName()));
        List<EventListener> eventListeners = changeListenerMap.get(t.getClass().getName());
        if(CollectionUtil.isEmpty(eventListeners)){
            log.warn("没有事件监听处理类");
            return;
        }
        if (async) {
            eventListeners.stream().
                    forEach((listener) -> executor.execute(() -> listener.onChange(t)));
        } else {
            eventListeners.stream().
                    forEach((listener) -> listener.onChange(t));
        }
    }

    public void publishEvent(T t) {
        publishEvent(t, true);
    }
    public void register(String modelClassName, EventListener eventListener){
        List<EventListener> eventListeners = changeListenerMap.get(modelClassName);
        if(eventListeners==null){
            eventListeners=new ArrayList<>();
            eventListeners.add(eventListener);
            changeListenerMap.put(modelClassName,eventListeners);
        } else if (!eventListeners.contains(eventListener)){
            eventListeners.add(eventListener);
        }
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, EventListener> beans = applicationContext.getBeansOfType(EventListener.class);
        for (Map.Entry<String, EventListener> entry : beans.entrySet()) {
            Class modelClass = TypeUtil.getFirstModelBySupperInterface(entry.getValue(), EventListener.class);
            register(modelClass.getName(),entry.getValue());
        }
    }
}
