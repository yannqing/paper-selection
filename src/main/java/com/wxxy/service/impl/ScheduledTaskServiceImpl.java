package com.wxxy.service.impl;

import com.wxxy.common.DateFormat;
import com.wxxy.service.ScheduledTaskService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {

    @Resource
    private TaskScheduler taskScheduler;

    /**
     * // 定义定时任务
     * @param taskName
     * @param secondTime
     * @param thirdTime
     */
    @Override
    public void scheduleTask(String taskName, String secondTime, String thirdTime) {
        // 执行任务逻辑
        Runnable SecondTask = this::SecondTask;
        // 执行任务逻辑
        Runnable ThirdTask = this::ThirdTask;
        // 使用 CronTrigger 设置定时任务的执行时间
        taskScheduler.schedule(SecondTask,
                new CronTrigger(Objects.requireNonNull(DateFormat.convertToCronExpression(secondTime))));
        taskScheduler.schedule(ThirdTask,
                new CronTrigger(Objects.requireNonNull(DateFormat.convertToCronExpression(thirdTime))));
    }

    public void SecondTask() {
        //1. 设置user表中，已选老师的学生status为1，队伍已满的老师maxNum为0

        //2. 将userTeam表中的所有未加入老师的数据删除

        //3. 将userTeam表中的所有已加入老师的数据存入redis，并删除

        //4. 调整所有老师的队伍限制/申请限制

        //5.
    }

    public void ThirdTask() {

    }

















}