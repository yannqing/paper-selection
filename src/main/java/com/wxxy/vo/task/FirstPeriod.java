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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Deprecated
//@Component
@Slf4j
public class FirstPeriod {

    @Resource
    private RedisCache redisCache;

    @Resource
    private ObjectMapper objectMapper;

    private static List<LocalDateTime> startTime; // 开始时间
    private static List<LocalDateTime> endTime;   // 结束时间

    private boolean isExecuteInit = false;  //初始化任务是否执行

    // 设置开始时间和结束时间
    public static void setTimePeriod(String firstStart, String firstEnd, String secondStart, String secondEnd, String thirdStart, String thirdEnd) {
        startTime = new ArrayList<>();
        endTime = new ArrayList<>();
        startTime.add(LocalDateTime.parse(firstStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        startTime.add(LocalDateTime.parse(secondStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        startTime.add(LocalDateTime.parse(thirdStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        endTime.add(LocalDateTime.parse(firstEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        endTime.add(LocalDateTime.parse(secondEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        endTime.add(LocalDateTime.parse(thirdEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Scheduled(cron = "0 * * * * ?")
    public void FirstTask() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        //获取登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

        for (int i = 0; i < 3; i++) {
            //1. 判断当前时间位于第几轮筛选
            if (currentTime.isAfter(endTime.get(i))) {
                continue;
            }else {
                System.out.println("当前是第"+i+"轮");
                //2. 如果在当前时间段
                if (currentTime.isAfter(startTime.get(i).minusSeconds(1)) && currentTime.isBefore(endTime.get(i).plusSeconds(1))) {
                    redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
                }else {
                    // 不在时间段内
                    redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
                    if (currentTime.isAfter(startTime.get(i).minusHours(1)) && currentTime.isBefore(startTime.get(i)) && !isExecuteInit) {
                        init();
                        isExecuteInit = true;
                    }
                }
            }
        }
        // 判断当前时间是否在第一阶段的时间段内
//        if (currentTime.isAfter(startTime.get(0).minusSeconds(1)) && currentTime.isBefore(endTime.get(0).plusSeconds(1))) {
//            // 在时间段内
////            if (!isExecute) {
//                redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
//                execute();
////                isExecute = true;
////            }
//        } else {
//            // 不在时间段内
//            if (userLoginIsRunning.equals("true")) {
//                redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
//            }
//            if (currentTime.isAfter(startTime.minusHours(1)) && currentTime.isBefore(startTime) && !isExecuteInit) {
//                init();
//                isExecuteInit = true;
//            }
//        }
    }

    public void init() {
        System.out.println("任务1初始化操作！");
    }

    @PostConstruct
    public void initTime() {
        //初始化时间
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        String firstBeginTime = null;
        String firstEndTime = null;
        String secondBeginTime = null;
        String secondEndTime = null;
        String thirdBeginTime = null;
        String thirdEndTime = null;
        if (scheduleTaskPeriod != null) {
            try {
                Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
                firstBeginTime = (String) map.get("firstBeginTime");
                firstEndTime = (String) map.get("firstEndTime");
                secondBeginTime = (String) map.get("secondBeginTime");
                secondEndTime = (String) map.get("secondEndTime");
                thirdBeginTime = (String) map.get("thirdBeginTime");
                thirdEndTime = (String) map.get("thirdEndTime");
            }catch (JsonProcessingException e) {
                log.error("第一阶段，初始化时间错误："+e.getMessage());
            }

        }
        else {
            firstBeginTime = "2124-12-12 12:00:00";
            firstEndTime = "2124-12-12 13:00:00";
            secondBeginTime = "2124-12-12 12:00:00";
            secondEndTime = "2124-12-12 13:00:00";
            thirdBeginTime = "2124-12-12 12:00:00";
            thirdEndTime = "2124-12-12 13:00:00";
        }
        setTimePeriod(firstBeginTime, firstEndTime, secondBeginTime, secondEndTime, thirdBeginTime, thirdEndTime);
        //初始化登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        if (userLoginIsRunning != null) {
            redisCache.deleteObject("UserLoginIsRunning");
        }
        LocalDateTime currentTime = LocalDateTime.now();
        redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);

//        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))){
//            redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
//        }else {
//            redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
//        }
    }
    public void execute() {
        System.out.println("任务1执行");
    }
}
