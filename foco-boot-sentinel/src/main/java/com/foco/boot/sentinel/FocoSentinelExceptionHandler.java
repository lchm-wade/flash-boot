package com.foco.boot.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.foco.context.util.ResponseUtils;
import com.foco.model.ApiResult;
import com.foco.model.constant.FocoErrorCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lucoo
 * @version 1.0.0
 * @Description sentinel异常统一处理 针对servlet环境
 * @date 2021-06-24 09:45
 */
public class FocoSentinelExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        ResponseUtils.write(200, JSON.toJSONString(ApiResult.error(FocoErrorCode.SENTINEL_FLOW_ERROR)));
    }
}
