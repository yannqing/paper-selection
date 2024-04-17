package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.vo.auth.LoginVo;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    LoginVo login(String username, String password, HttpServletRequest request) throws JsonProcessingException;

    void logout(HttpServletRequest request);
}
