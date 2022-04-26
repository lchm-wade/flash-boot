package com.foco.boot.arthas.spring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foco.boot.arthas.constant.ArthasClientConstant;
import com.foco.boot.arthas.utils.GetRealLocalIP;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.foco.boot.arthas.constant.ArthasClientConstant.DEFAULT_FORBIDDEN;

/**
 * @author ChenMing
 * @date 2022/4/6
 */
@ConfigurationProperties(prefix = ArthasProperties.PREFIX)
@Data
@Slf4j
public class ArthasProperties {

    public static final String PREFIX = "foco.arthas";

    /**
     * TODO 只是为"ip+port"的agentId唯一性标记，不作功能使用
     * 如果由于环境复杂导致无法自动获取，请自设
     */
    private String agentIp = GetRealLocalIP.getLocalIP();

    /**
     * TODO 只是为"ip+port"的agentId唯一性标记，不作功能使用
     * 默认取${server.port}没有则为{@link ArthasClientConstant.DEFAULT_PORT}，如果不想按此标准，请自设
     */
    private String agentPort;

    /**
     * 用作服务端的ip（代指本机接收命令，自己作为服务端），默认localhost
     */
    private String ip;

    /**
     * 如果配置 telnetPort为 -1 ，则不listen telnet端口。httpPort类似。
     *
     * <p>如果配置 telnetPort为 0 ，则随机telnet端口，
     * 在~/logs/arthas/arthas.log里可以找到具体端口日志。httpPort类似</>
     */
    private int telnetPort;

    /**
     * @see #telnetPort 规则看此变量注释
     */
    private int httpPort;

    /**
     * 业务线分组名称
     */
//    private String group;

    /**
     * 环境，不配置默认取${spring.profiles.active}
     * 都没有则为{@link ArthasClientConstant#DEFAULT}的字符串
     */
//    private String env;

    /**
     * 应用名，默认会取${spring.application.name}
     */
    private String appName;

    /**
     * tunnelServer服务器地址
     * 例：'ws://127.0.0.1:7777/ws' 端口运维会域名映射，研发无需考虑
     */
    private String tunnelServer;

    /**
     * 心跳上报地址
     */
    private String heartbeatAddress;

    /**
     * 提前启动Arthas
     * true：容器加载的时候就启动arthas  false：等服务端的通知（默认false）
     */
    private boolean preStart;

    /**
     * 心跳间隔  单位/秒
     * <p>此项会根据{@link ApplicationRegister}中的心跳上报后响应进行
     * Arthas服务台开启，所以需要注意间隔时间，否则服务端将会等待很久
     *
     * <p>为什么用此字段做服务台开启标识媒介？主要在于不需要考虑服务端的逆
     * 向通讯，成本低，也不需要考虑维护长链接的方式，并且目前仅用了jdk原生
     * API，非常轻量
     */
    private int heartbeatInterval = 3;

    /**
     * report executed command
     */
    private String statUrl;

    /**
     * session timeout seconds
     */
    private long sessionTimeout;

    private String home;

    /**
     * when arthas agent init error will throw exception by default.
     */
    private boolean slientInit = false;

    /**
     * 需要禁用的命令行，例如（stop）等，含有多个则“,”分割
     */
    private String disabledCommands;

    @JsonIgnore
    @Resource
    private Environment environment;

    @PostConstruct
    public void init() {
        if (StringUtils.hasLength(getDisabledCommands())) {
            setDisabledCommands(DEFAULT_FORBIDDEN + "," + getDisabledCommands());
        } else {
            setDisabledCommands(DEFAULT_FORBIDDEN);
        }
//        String hint = "Arthas的{}未配置，将不会启用Arthas懒加载功能";
//        if (!StringUtils.hasLength(getGroup())) {
//            log.warn(hint.replace("{}", "group（业务线分组名称）"));
//        }
//        if (!StringUtils.hasLength(getHeartbeatAddress())) {
//            log.warn(hint.replace("{}", "heartbeatAddress（服务端地址）"));
//        } else {
//            boolean hasProtocol = getHeartbeatAddress().startsWith("http");
//            if (!hasProtocol) {
//                hasProtocol = getHeartbeatAddress().startsWith("https");
//            }
//            if (!hasProtocol) {
//                setHeartbeatAddress("http://" + getHeartbeatAddress() + "/heartbeat");
//            }
//        }
//        if (!StringUtils.hasLength(env)) {
//            env = environment.getProperty("spring.profiles.active", ArthasClientConstant.DEFAULT);
//        }
        if (agentPort == null) {
            agentPort = environment.getProperty("server.port", ArthasClientConstant.DEFAULT_PORT);
        }
    }
}
