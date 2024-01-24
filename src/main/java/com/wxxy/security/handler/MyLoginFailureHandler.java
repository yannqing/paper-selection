package com.wxxy.security.handler;

import cn.hutool.json.JSONUtil;
import com.wxxy.common.Code;
import com.wxxy.utils.ResultUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

public class MyLoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(JSONUtil.toJsonStr(ResultUtils.failure(Code.LOGIN_FAILURE,null,"用户名或密码错误！")));
        exception.printStackTrace();
    }
}
