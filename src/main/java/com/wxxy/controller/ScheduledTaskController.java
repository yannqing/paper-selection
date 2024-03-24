package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.service.ScheduledTaskService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;
import java.util.Map;

@RestController
@RequestMapping("/schedule")
public class ScheduledTaskController {

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @PostMapping("/set")
    public BaseResponse<Object> setFirstTime(String firstTime, String secondTime, String thirdTime) {
        scheduledTaskService.scheduleTask(firstTime, secondTime, thirdTime);
        return ResultUtils.success();
    }

    /**
     * 设置定时任务
     * @param firstBeginTime
     * @param firstEndTime
     * @param secondBeginTime
     * @param secondEndTime
     * @param thirdBeginTime
     * @param thirdEndTime
     * @param request
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/setTime")
    public BaseResponse<Object> scheduleTask(String firstBeginTime, String firstEndTime, String secondBeginTime, String secondEndTime, String thirdBeginTime, String thirdEndTime, HttpServletRequest request) throws JsonProcessingException {
        scheduledTaskService.scheduleTask(firstBeginTime, firstEndTime, secondBeginTime, secondEndTime, thirdBeginTime, thirdEndTime, request);

        return ResultUtils.success(Code.SUCCESS, null, "设置定时任务的时间成功！");
    }

    @GetMapping("/getTime")
    public BaseResponse<Map> getScheduleTasks(HttpServletRequest request) throws JsonProcessingException {
        Map scheduleTasks = scheduledTaskService.getScheduleTasks(request);
        if (scheduleTasks == null) {
            return ResultUtils.success(Code.GET_SCHEDULE_TIME_FAILURE, null , "未设置定时任务");
        }
        return ResultUtils.success(Code.SUCCESS, scheduleTasks, "获取定时任务的时间成功");
    }
}
