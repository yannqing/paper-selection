package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.common.DateFormat;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.ScheduledTaskService;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.task.FirstPeriod;
import com.wxxy.vo.task.SecondPeriod;
import com.wxxy.vo.task.ThirdPeriod;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ScheduledTaskServiceImpl implements ScheduledTaskService {

    @Resource
    private TaskScheduler taskScheduler;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private RedisCache redisCache;

    /**
     * // 定义定时任务
     * @param secondTime
     * @param thirdTime
     */
    @Override
    public void scheduleTask(String firstTime, String secondTime, String thirdTime) {
//        // 执行任务逻辑
//        Runnable FirstTask = this::FirstTask;
//        // 执行任务逻辑
//        Runnable SecondTask = () -> {
//            try {
//                SecondTask();
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException("第一轮结束后的行为报错："+e.getMessage());
//            }
//        };
//        // 执行任务逻辑
//        Runnable ThirdTask = this::ThirdTask;
//        // 使用 CronTrigger 设置定时任务的执行时间
//        taskScheduler.schedule(FirstTask,
//                new CronTrigger(Objects.requireNonNull(DateFormat.convertToCronExpression(firstTime))));
//        taskScheduler.schedule(SecondTask,
//                new CronTrigger(Objects.requireNonNull(DateFormat.convertToCronExpression(secondTime))));
//        taskScheduler.schedule(ThirdTask,
//                new CronTrigger(Objects.requireNonNull(DateFormat.convertToCronExpression(thirdTime))));
    }

    @Override
    public void scheduleTask(String firstBeginTime, String firstEndTime, String secondBeginTime, String secondEndTime, String thirdBeginTime, String thirdEndTime, HttpServletRequest request) throws JsonProcessingException {
        //设置各个阶段的定时任务
        FirstPeriod.setTimePeriod(firstBeginTime, firstEndTime);
        SecondPeriod.setTimePeriod(secondBeginTime, secondEndTime);
        ThirdPeriod.setTimePeriod(thirdBeginTime, thirdEndTime);
        //将时间存入redis
        Map<String, String> scheduleTimePeriod = new HashMap<String, String>();
        scheduleTimePeriod.put("firstPeriodBeginTime", firstBeginTime);
        scheduleTimePeriod.put("firstPeriodEndTime", firstEndTime);
        scheduleTimePeriod.put("secondPeriodBeginTime", secondBeginTime);
        scheduleTimePeriod.put("secondPeriodEndTime", secondEndTime);
        scheduleTimePeriod.put("thirdPeriodBeginTime", thirdBeginTime);
        scheduleTimePeriod.put("thirdPeriodEndTime", thirdEndTime);

        String scheduleTaskTime = objectMapper.writeValueAsString(scheduleTimePeriod);
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        if (scheduleTaskPeriod != null) {
            redisCache.deleteObject("scheduleTaskPeriod");
        }
        redisCache.setCacheObject("scheduleTaskPeriod", scheduleTaskTime, 60*60*24*30, TimeUnit.SECONDS);
        log.info("设置定时任务成功！{}", scheduleTimePeriod);
    }

    @Override
    public Map getScheduleTasks(HttpServletRequest request) throws JsonProcessingException {
        SecondServiceImpl.checkRole(request);

        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        if (scheduleTaskPeriod != null) {
            Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
            return map;
        }

        log.info("管理员查询定时任务！");
        return null;
    }

//
//    @Scheduled(cron = "0 * * * * ?")
//    public void FirstTask() {
//
//    }

//    @Scheduled(cron = "0 * * * * ?")
//    public void SecondTask() throws JsonProcessingException {
//        //0. 将第一轮的老师数据存入redis中
//        SecondStorageTeacherMessage();
//        //1. 设置user表中，已选老师的学生status为1，队伍已满的老师maxNum为0
//        SecondChangeUserStatus();
//        //2. 将userTeam表中的所有未加入老师的数据删除，更新队伍表中的申请数量
//        SecondDeleteNotJoined();
//        //3. 调整所有老师的队伍限制/申请限制
//        SecondChangeMax();
//        //4. 将userTeam表中的所有已加入老师的数据存入redis，并删除，更新队伍表的当前数量
//        SecondStorage();
//        //5.
//    }

//    @Scheduled(cron = "0 * * * * ?")
//    public void ThirdTask() {
//
//    }

    /**
     * 0. 将第一轮的老师数据存入redis中
     */
    public void SecondStorageTeacherMessage() throws JsonProcessingException {
        //1. 获取第一轮所有老师的数据
        List<Teacher> teachers = teacherMapper.selectList(null);
        //2. 存入redis
        String teacherMessage = objectMapper.writeValueAsString(teachers);
        redisCache.setCacheObject("firstResult:teacherMessage", teacherMessage);

    }

    /**
     * 1. 设置user表中，已选老师的学生status为1，队伍已满的老师maxNum为0
     */
    public void SecondChangeUserStatus() {
        //1. 查询所有已经选择老师的学生
        List<UserTeam> joinedUsers = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("isJoin", 1));
        for (UserTeam userTeam : joinedUsers) {
            //给user的status字段设为1
            userMapper.update(new UpdateWrapper<User>()
                    .eq("id", userTeam.getUserId())
                    .set("userStatus", 1));
        }
        // 排除管理员
        userMapper.update(new UpdateWrapper<User>().eq("userRole", 1).set("userStatus", 0));

        //2. 给第一轮队伍已满teacher的maxNum设为0
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher : teachers) {
            if (Objects.equals(teacher.getCurrentNum(), teacher.getMaxNum())) {
                teacherMapper.update(new UpdateWrapper<Teacher>().eq("id", teacher.getId()).set("maxNum", 0));
            }
        }
    }

    /**
     * 2. 将userTeam表中的所有未加入老师的数据删除
     */
    public void SecondDeleteNotJoined() {
        //1. 删除未加入的数据
        userTeamMapper.delete(new QueryWrapper<UserTeam>().eq("isJoin", 0));
        //2. 更新队伍表的申请数量全为0
        teacherMapper.update(new UpdateWrapper<Teacher>().set("applyNum", 0));
    }


    /**
     * 3. 调整所有老师的队伍限制/申请限制
     */
    public void SecondChangeMax() {
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher: teachers) {
            if (teacher.getMaxNum() != 0) {
                Integer maxNum = teacher.getMaxNum();
                Integer currentNum = teacher.getCurrentNum();
                teacherMapper.update(new UpdateWrapper<Teacher>()
                        .eq("id", teacher.getId())
                        .set("maxNum", maxNum-currentNum).set("MaxApply", maxNum-currentNum));
            }
        }
    }

    /**
     * 4. 将userTeam表中的所有已加入老师的数据存入redis，并删除
     * @throws JsonProcessingException
     */
    public void SecondStorage() throws JsonProcessingException {
        //1. 查询所有已加入的信息
        List<UserTeam> joinedMessage = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("isJoin", 1));
        //2. 存入redis，一年过期
        String firstResult = objectMapper.writeValueAsString(joinedMessage);
        redisCache.setCacheObject("firstResult:joinedMessage", firstResult, 60*60*24*365, TimeUnit.SECONDS);
        //3. 删除数据
        userTeamMapper.delete(new QueryWrapper<UserTeam>().eq("isJoin", 1));
        //4. 更新队伍表的队伍数据全为0
        teacherMapper.update(new UpdateWrapper<Teacher>().set("currentNum", 0));
    }


















}