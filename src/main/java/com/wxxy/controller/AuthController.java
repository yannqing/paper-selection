package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.service.AuthService;
import com.wxxy.service.UserService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.auth.LoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "认证接口")
public class AuthController {
    @Resource
    private AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public BaseResponse<LoginVo> login(String username, String password, HttpServletRequest request) throws JsonProcessingException {
        if (username == null || password == null) {
            throw new IllegalArgumentException("参数为空");
        }
        LoginVo login = authService.login(username, password, request);
        if (login == null) {
//            throw new IllegalStateException("用户名或密码错误");
            return ResultUtils.failure(Code.LOGIN_FAILURE, null, "用户名或密码错误");
        }
        return ResultUtils.success(Code.LOGIN_SUCCESS, login, "登录成功");
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public BaseResponse<Object> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResultUtils.success(Code.LOGOUT_SUCCESS, null, "退出登录成功");
    }
}
