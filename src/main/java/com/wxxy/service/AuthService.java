package com.wxxy.service;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    <T> Object login(String username, String password, HttpServletRequest request);
}
