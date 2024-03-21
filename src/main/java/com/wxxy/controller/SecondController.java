package com.wxxy.controller;

import com.wxxy.vo.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/second")
public class SecondController {

    @GetMapping("/getAllTeachers")
    public BaseResponse studentGetAllTeachers(HttpServletRequest request) {
        return null;
    }
}
