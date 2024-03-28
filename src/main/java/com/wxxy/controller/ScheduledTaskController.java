package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.service.ScheduledTaskService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.PostConstruct;
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
     * @param beginTime
     * @param offTime
     * @param request
     * @return
     * @throws JsonProcessingException
     */
    @PostMapping("/setTime")
    public BaseResponse<Object> scheduleTask(String beginTime, String offTime, HttpServletRequest request) throws JsonProcessingException {
        scheduledTaskService.scheduleTask(beginTime, offTime, request);

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

    @PostMapping("/forbidden")
    public BaseResponse<Object> forbidden(HttpServletRequest request) {
        scheduledTaskService.forbidden(request);
        return ResultUtils.success(Code.SUCCESS, null, "已禁止已选学生和队伍已满老师登录");
    }

    @PostMapping("/updateSize")
    public BaseResponse<Object> updateSize(HttpServletRequest request) {
        scheduledTaskService.updateSize(request);
        return ResultUtils.success(Code.SUCCESS, null, "等额修改数量成功");
    }
}
