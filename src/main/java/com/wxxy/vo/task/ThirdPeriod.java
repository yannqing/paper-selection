package com.wxxy.vo.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.utils.RedisCache;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ThirdPeriod {

    @Resource
    private RedisCache redisCache;

    @Resource
    private ObjectMapper objectMapper;

    private static LocalDateTime startTime; // 开始时间
    private static LocalDateTime endTime;   // 结束时间

    private boolean isExecuteInit = false;  //初始化任务是否执行

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
                redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
                execute();
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
        System.out.println("任务3初始化操作！");
    }

    @PostConstruct
    public void initTime() {
        //初始化时间
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        String beginTime = null;
        String offTime = null;
        if (scheduleTaskPeriod != null) {
            try {
                Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
                beginTime = (String) map.get("beginTime");
                offTime = (String) map.get("offTime");
            }catch (JsonProcessingException e) {
                log.error("初始化时间错误："+e.getMessage());
            }

        }
        else {
            beginTime = "2124-12-12 12:00:00";
            offTime = "2124-12-12 13:00:00";
        }
        setTimePeriod(beginTime, offTime);
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
        System.out.println("任务3执行");
    }
}
