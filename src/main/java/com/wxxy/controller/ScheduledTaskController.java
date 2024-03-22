package com.wxxy.controller;

import com.wxxy.service.ScheduledTaskService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;

@RestController
@RequestMapping("/schedule")
public class ScheduledTaskController {

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/setTime")
    public BaseResponse<Object> setTime(String firstTime, String secondTime, String thirdTime) {
        scheduledTaskService.scheduleTask(firstTime, secondTime, thirdTime);
        return ResultUtils.success();
    }
}
