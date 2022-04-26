package com.foco.boot.web.interceptor;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foco.context.util.HttpContext;
import com.foco.context.util.ResponseUtils;
import com.foco.model.ApiResult;
import com.foco.model.constant.ExceptionHandlerOrderConstants;
import com.foco.model.constant.FocoErrorCode;
import com.foco.model.exception.ApiException;
import com.foco.model.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description 全局异常处理
 * @Author lucoo
 * @Date 2021/6/23 18:55
 **/
@Slf4j
@RestControllerAdvice
@Order(ExceptionHandlerOrderConstants.WEB)
@ConditionalOnProperty(prefix = "foco.global.exception",name = "enabled",matchIfMissing = true)
public class GlobalExceptionHandler {
    /***
     *  业务异常拦截
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = ApiException.class)
    public ApiResult apiExceptionHandler(HttpServletRequest req,ApiException e) {
        ApiResult apiResult = ApiResult.error(e.getCode(), e.getMessage(),e.getParams());
        log.warn("---apiExceptionHandler--- Host:{},invokes url:{},[errorCode:{},msg:{}]", req.getRemoteHost(),
                req.getRequestURL(),apiResult.getCode(), apiResult.getMsg());
        return apiResult;
    }
    /***
     *  系统异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = SystemException.class)
    public ApiResult systemExceptionHandler(HttpServletRequest req,SystemException e) {
        ApiResult apiResult = ApiResult.error(e.getCode(), e.getMessage());
        log.warn("---systemExceptionHandler--- Host:{},invokes url:{},[errorCode:{},msg:{}]", req.getRemoteHost(),
                req.getRequestURL(),apiResult.getCode(), apiResult.getMsg());
        return apiResult;
    }
    /***
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public ApiResult noHandlerFoundExceptionHandler(HttpServletRequest req, NoHandlerFoundException e) {
        log.warn("---NoHandlerFoundExceptionHandler--- Host: {} invokes url: {}, message: {}", req.getRemoteHost(),
                req.getRequestURL(), e.getMessage());
        return ApiResult.error(FocoErrorCode.PATH_ERROR);
    }
    /**
     * 未知异常拦截
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Throwable.class)
    public ApiResult defaultExceptionHandler(HttpServletRequest req, Throwable e) {
        Throwable t=ExceptionUtil.getRootCause(e);
        log.error("---DefaultExceptionHandler--- Host: {} invokes url: {}, message:{}, error: {}", req.getRemoteHost(),
                req.getRequestURL(), t.getMessage(), e);
        return ApiResult.error(FocoErrorCode.SYSTEM_ERROR);
    }
}
