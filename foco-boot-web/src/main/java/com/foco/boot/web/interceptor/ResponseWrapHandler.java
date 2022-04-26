package com.foco.boot.web.interceptor;

import com.foco.context.util.HttpContext;
import com.foco.context.util.PathMatchUtil;
import com.foco.model.ApiResult;
import com.foco.model.annotation.SkipWrap;
import com.foco.model.constant.MainClassConstant;
import com.foco.model.constant.ResponseBodyOrderConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Description TODO
 * @Author lucoo
 * @Date 2021/6/10 10:34
 **/
@RestControllerAdvice
@Order(ResponseBodyOrderConstants.WARP_BODY)
@ConditionalOnProperty(prefix = "foco.response.wrap",name = "enabled",matchIfMissing = true)
@Slf4j
public class ResponseWrapHandler implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResult
                ||returnType.hasMethodAnnotation(SkipWrap.class)
                ||body instanceof String
                ||HttpContext.isFeignRequest()
                || PathMatchUtil.match(MainClassConstant.SWAGGER_URL, HttpContext.getRequest().getServletPath())
                ){
            return body;
        }
        return ApiResult.success(body);
    }
}
