package com.foco.boot.arthas.spring;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.foco.boot.arthas.attach.ArthasAgent;
import com.foco.boot.arthas.constant.ArthasClientConstant;
import com.foco.boot.arthas.utils.OkHttpUtils;
import com.foco.boot.arthas.utils.ScheduleThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ChenMing
 * @date 2022/4/7
 */
@Slf4j
public class ApplicationRegister implements ApplicationListener<ApplicationReadyEvent> {

    private final ArthasProperties properties;

    private final ArthasAgent arthasAgent;

    private final Map<String, String> arthasConfigMap;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new ScheduleThreadFactory("Arthas-Client-Heartbeat"));

    public ApplicationRegister(ArthasProperties properties, ArthasAgent arthasAgent) {
        this.properties = properties;
        this.arthasAgent = arthasAgent;
        this.arthasConfigMap = arthasAgent.getConfigMap();
    }

    private Heartbeat heartbeat;

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (StringUtils.hasLength(properties.getHeartbeatAddress())) {
            if (heartbeat == null) {
                this.heartbeat = new Heartbeat(arthasConfigMap);
            }
            heartbeat.run();
        }
    }

    @Data
    private final class Heartbeat implements Runnable {

        public Heartbeat(Map<String, String> param) {
            this.param = param;
        }

        private Map<String, String> param;

        /**
         * 是否初始化了（初始化注册时全量推送，后只推agentId仅维护心跳减少带宽消耗）
         */
        private boolean initialized;

        private String errorMsg = "";

        @Override
        public void run() {
//            String response = "";
//            try {
//                OkHttpUtils httpUtils = OkHttpUtils.builder().addHeader(OkHttpUtils.CONTENT_TYPE, OkHttpUtils.APPLICATION_JSON).url(properties.getHeartbeatAddress());
//                if (initialized) {
//                    httpUtils.addParam(ArthasClientConstant.ARTHAS_PREFIX + ArthasClientConstant.AGENT_ID, arthasAgent.getAgentId());
//                } else {
//                    for (String key : param.keySet()) {
//                        httpUtils.addParam(key, param.get(key));
//                    }
//                    initialized = true;
//                }
//                response = httpUtils.syncPost();
//                try {
//                    List<ArthasSignalType> signals = JSONObject.parseArray(response, ArthasSignalType.class);
//                    if (!CollectionUtils.isEmpty(signals)) {
//                        //NONE信号可以忽略，绝大多数是这种情况
//                        for (ArthasSignalType signal : signals) {
//                            try {
//                                switch (signal) {
//                                    case STOP -> arthasAgent.destroy();
//                                    case START -> arthasAgent.init();
//                                    //置为未初始化则会重新全量推送
//                                    case INFO_AGAIN -> initialized = false;
//                                }
//                            } catch (Throwable e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    if (StringUtils.hasLength(errorMsg)) {
//                        log.info("Arthas心跳请求恢复正常");
//                        errorMsg = "";
//                    }
//                } catch (JSONException e) {
//                    JSONObject object = JSONObject.parseObject(response);
//                    //删除时间戳因素
//                    object.remove("timestamp");
//                    response = object.toString();
//                    //仅打印一次，不做过多的重复打印
//                    if (!errorMsg.equals(response)) {
//                        log.warn("Arthas心跳请求异常响应：" + response);
//                        errorMsg = response;
//                    }
//                }
//            } catch (Throwable e) {
//                //仅打印一次，不做过多的重复打印
//                if (!errorMsg.equals(e.getMessage())) {
//                    log.warn("Arthas心跳请求异常：" + e + "，响应：" + response);
//                    errorMsg = e.getMessage();
//                }
//            } finally {
//                executor.schedule(this, properties.getHeartbeatInterval(), TimeUnit.SECONDS);
//            }
        }

    }
}
