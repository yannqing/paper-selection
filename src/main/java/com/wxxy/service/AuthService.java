package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    Object login(String username, String password, HttpServletRequest request) throws JsonProcessingException;

    void logout(HttpServletRequest request);
}
