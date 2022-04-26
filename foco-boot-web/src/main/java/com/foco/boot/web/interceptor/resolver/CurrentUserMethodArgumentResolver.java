package com.foco.boot.web.interceptor.resolver;

import com.foco.context.core.LoginContext;
import com.foco.context.core.LoginContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @Description 为加了CurrentUser注解的方法自动注入值
 * @Author lucoo
 * @Date 2021/6/10 15:21
 **/
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return LoginContext.class.isAssignableFrom(parameter.getParameterType())
                &&parameter.getParameter().isAnnotationPresent(CurrentUser.class);
    }
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Class<LoginContext> parameterType = (Class<LoginContext>) parameter.getParameterType();
        LoginContext loginContext = LoginContextHolder.getLoginContext(parameterType);
        return loginContext;
    }
}
