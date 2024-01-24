package com.wxxy.controller;

import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {


    @GetMapping("/text")
    public BaseResponse text(){
        return ResultUtils.success();
    }
}
