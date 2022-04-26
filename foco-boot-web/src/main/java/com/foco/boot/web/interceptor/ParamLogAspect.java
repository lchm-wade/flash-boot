package com.foco.boot.web.interceptor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foco.boot.web.properties.ParamLogProperties;
import com.foco.context.util.HttpContext;
import com.foco.model.constant.AopOrderConstants;
import com.foco.model.constant.FocoConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StopWatch;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> 记录RestController 方法执行的输入/输出 </p>
 *
 * @Author lucoo
 * @Date 2021/6/26 15:25
 */
@Slf4j
@Aspect
@Order(AopOrderConstants.PARAM_LOG)
public class ParamLogAspect {
    @Autowired
    private ParamLogProperties paramLogProperties;
    LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    @Around(value = "@within(org.springframework.web.bind.annotation.RestController)||@annotation(com.foco.context.annotation.PretaMethod)")
    public Object observer(ProceedingJoinPoint pjp) throws Throwable {
        //打印入参
        InfoData param = null;
        try {
            param = stdIn(pjp);
            if(param!=null&&log.isInfoEnabled()){
                log.info("url: {}, method:{},args:[{}]", HttpContext.getRequestUrl(), param.getMethod(), param.getInfo());
            }
        } catch (Exception exception) {
            log.warn("打印入参异常", exception);
        }
        StopWatch watch = new StopWatch();
        watch.start();
        Object result = null;
        Throwable throwable = null;
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            throwable = t;
            throw t;
        } finally {
            watch.stop();
            if (param != null) {
                if(log.isInfoEnabled()){
                    log.info("url: {}, method:{},执行方法消耗:{}毫秒", HttpContext.getRequestUrl(), param.getMethod(), watch.getTotalTimeMillis());
                }
                if (StrUtil.isNotBlank(param.getUpLoadInfo())) {
                    ActiveSpan.tag(FocoConstants.SKY_WALKING_LOG_TAG, param.getUpLoadInfo());
                }
            } else {
                if(log.isInfoEnabled()){
                    log.info("url:{},执行方法消耗:{}毫秒", HttpContext.getRequestUrl(), watch.getTotalTimeMillis());
                }
            }
            //打印出参
            try {
                stdOutOrError(throwable, result, pjp);
            } catch (Exception exception) {
                log.warn("打印出参异常", exception);
            }
        }
        return result;
    }

    private void stdOutOrError(Throwable throwable, Object result, ProceedingJoinPoint pjp) {
        if (!paramLogProperties.getIsPrintOut()) {
            //不打印出参
            return;
        }
        if (throwable == null) {
            stdOut(result, pjp);
        } else {
            stdError(throwable, result);
        }
    }

    private void stdOut(Object result, ProceedingJoinPoint pjp) {
        //过滤输出项
        Signature signature = pjp.getSignature();
        if (signature == null) {
            log.warn("signature is null,should fix quickly.");
        } else {
            String controller = signature.getDeclaringTypeName();
            String method = controller + "." + signature.getName();
            if (outFilter(controller, method)) {
                log.warn(method + " can not print out.");
                return;
            }
        }
        if (log.isInfoEnabled()) {
            log.info("result:" + (result == null ? "null" : print(result)));
        }
    }

    private void stdError(Throwable throwable, Object result) {
        log.error("throwable:" + throwable.getMessage() + ",result:" + (result == null ? "null" : print(result)), throwable);
    }

    private InfoData stdIn(ProceedingJoinPoint pjp) {
        if (!paramLogProperties.getIsPrintIn()) {
            //不打印入参
            return null;
        }
        if (pjp == null) {
            log.warn("ProceedingJoinPoint is null,should fix quickly.");
            return null;
        }
        Signature signature = pjp.getSignature();
        String controller;
        String method;
        if (signature == null) {
            log.warn("signature is null,should fix quickly.");
            return null;
        } else {
            controller = signature.getDeclaringTypeName();
            method = controller + "." + signature.getName();
            if (inFilter(controller, method)) {
                log.warn(method + " can not print in .");
                return null;
            }
        }
        Object[] args = pjp.getArgs();
        if (args == null) {
            log.warn("args is null,should fix quickly.");
            return null;
        }
        int argsLength = args.length;
        if (argsLength <= 0) {
            log.info("args is empty.");
            return null;
        }
        StringBuilder builder = new StringBuilder();
        StringBuilder uploadBuilder = new StringBuilder();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(((MethodSignature)pjp.getSignature()).getMethod());
        for (int i = 0; i < argsLength; i++) {
            String logInfo = print(args[i]);
            if (!"null".equals(logInfo)) {
                builder.append("{arg:");
                builder.append(parameterNames[i]);
                builder.append(",value:");
                builder.append(logInfo);
                builder.append("},");
                uploadBuilder.append(logInfo);
            }
        }
        String info=builder.substring(0, builder.length() - 1);
        if (paramLogProperties.isUploadSkyWalking()) {
            return new InfoData(method, info, uploadBuilder.substring(0, Math.min(paramLogProperties.getLogLength(), uploadBuilder.length())));
        }
        return new InfoData(method, info, null);
    }

    private String print(Object object) {
        if (object == null) {
            return "null";
        }
        if (object instanceof File) {
            return "null";
        }
        if (object instanceof MultipartFile) {
            return "null";
        }
        if (object instanceof InputStream || object instanceof OutputStream
                || object instanceof Reader || object instanceof Writer) {
            return "null";
        }
        if (object instanceof ServletRequest || object instanceof ServletResponse) {
            return "null";
        }
        if (object instanceof BindingResult) {
            return "null";
        }
        if (object instanceof InputStreamSource) {
            return "null";
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return JSON.toJSONString(object);
        }
    }

    public Boolean inFilter(String controller, String methodName) {
        if (paramLogProperties.getInFilter() != null && paramLogProperties.getInFilter().getMethods() != null) {
            List<String> controllers = paramLogProperties.getInFilter().getClassNames();
            if (controllers != null) {
                for (String s : controllers) {
                    if (s.equals(controller)) {
                        return true;
                    }
                }
            }
            List<String> methods = paramLogProperties.getInFilter().getMethods();
            if (methods != null) {
                for (String method : methods) {
                    if (method.equals(methodName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Boolean outFilter(String controller, String methodName) {
        if (paramLogProperties.getOutFilter() != null && paramLogProperties.getOutFilter().getMethods() != null) {
            List<String> controllers = paramLogProperties.getOutFilter().getClassNames();
            if (controllers != null && controllers.size() > 0) {
                for (String s : controllers) {
                    if (s.equals(controller)) {
                        return true;
                    }
                }
            }
            List<String> methods = paramLogProperties.getOutFilter().getMethods();
            if (methods != null && methods.size() > 0) {
                for (String method : methods) {
                    if (method.equals(methodName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    class InfoData {
        String method;
        String info;
        String upLoadInfo;

        public InfoData(String method, String info, String upLoadInfo) {
            this.method = method;
            this.info = info;
            this.upLoadInfo = upLoadInfo;
        }

        public String getUpLoadInfo() {
            return upLoadInfo;
        }

        public void setUpLoadInfo(String upLoadInfo) {
            this.upLoadInfo = upLoadInfo;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
