package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.service.AuthService;
import com.wxxy.service.UserService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Resource
    private AuthService authService;
    @PostMapping("/login")
    public BaseResponse<Object> login(String username, String password, HttpServletRequest request) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("参数为空");
        }
        Object login = authService.login(username, password, request);
        if (login == null) {
//            throw new IllegalStateException("用户名或密码错误");
            return ResultUtils.failure(Code.LOGIN_FAILURE, null, "用户名或密码错误");
        }
        return ResultUtils.success(Code.LOGIN_SUCCESS, login, "登录成功");
    }
}
