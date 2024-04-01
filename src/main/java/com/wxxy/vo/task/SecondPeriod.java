package com.wxxy.vo.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.utils.RedisCache;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Deprecated
//@Component
@Slf4j
public class SecondPeriod {

    @Resource
    private RedisCache redisCache;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private ObjectMapper objectMapper;

    private static LocalDateTime startTime; // 开始时间
    private static LocalDateTime endTime;   // 结束时间

    private boolean isExecuteInit = false;  //初始化任务是否执行
    private boolean isExecute = false;      //任务是否执行

    // 设置开始时间和结束时间
    public static void setTimePeriod(String start, String end) {
        startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Scheduled(cron = "0 * * * * ?")
    public void FirstTask() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        //获取登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

        // 判断当前时间是否在指定的时间段内
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))) {
            // 在时间段内
            if (!isExecute){
                redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
                execute();
                isExecute = true;
            }
        } else {
            // 不在时间段内
            if (userLoginIsRunning.equals("true")) {
                redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);

            }
            if (currentTime.isAfter(startTime.minusHours(1)) && currentTime.isBefore(startTime) && !isExecuteInit) {
                init();
                isExecuteInit = true;
            }
        }
    }

    public void init() {
        System.out.println("任务2初始化操作！");

        //1. 设置user表中，已选老师的学生status为1，队伍已满的老师maxNum为0
        SecondChangeUserStatus();
        //2. 将userTeam表中的所有未加入老师的数据删除，更新队伍表中的申请数量
        SecondDeleteNotJoined();
        //3. 调整所有老师的队伍限制/申请限制
        SecondChangeMax();
    }

    @PostConstruct
    public void initTime() {
        //初始化时间
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        String secondBeginTime = null;
        String secondEndTime = null;
        if (scheduleTaskPeriod != null) {
            try {
                Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
                secondBeginTime = (String) map.get("secondBeginTime");
                secondEndTime = (String) map.get("secondEndTime");
            }catch (JsonProcessingException e) {
                log.error("第二阶段，初始化时间错误："+e.getMessage());
            }
        }
        else {
            secondBeginTime = "2124-12-12 12:00:00";
            secondEndTime = "2124-12-12 13:00:00";
        }
        setTimePeriod(secondBeginTime, secondEndTime);
        //初始化登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        if (userLoginIsRunning != null) {
            redisCache.deleteObject("UserLoginIsRunning");
        }
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))){
            redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
        }else {
            redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
        }
    }
    public void execute() {
        System.out.println("任务2执行");
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

        //2. 给第一轮队伍已满teacher的status设为1
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher : teachers) {
            if (Objects.equals(teacher.getCurrentNum(), teacher.getMaxNum())) {
                teacherMapper.update(new UpdateWrapper<Teacher>().eq("id", teacher.getId()).set("status", 1));
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
}
