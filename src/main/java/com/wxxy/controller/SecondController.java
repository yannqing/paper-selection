package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.service.SecondService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/second")
public class SecondController {

    @Resource
    private SecondService secondService;

    @PostMapping("/login")
    public BaseResponse<Object> login(String username, String password, HttpServletRequest request){
        Object login = secondService.login(username, password, request);
        return ResultUtils.success(Code.LOGIN_SUCCESS, login, "登录成功");
    }




}
