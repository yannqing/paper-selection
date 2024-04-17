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
import java.util.HashMap;
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
    public void FirstTask() throws JsonProcessingException {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();
        //获取登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

//        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // 判断当前时间是否在指定的时间段内
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))) {
            // 在时间段内
            isExecuteInit = false;
            redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
                execute();
        } else {
            // 不在时间段内



            if (userLoginIsRunning.equals("true")) {
                redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
            }
            if (currentTime.isAfter(endTime) && !isExecuteInit) {

                init();
                isExecuteInit = true;
            }

        }
    }

    public void init() throws JsonProcessingException {
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        Map map = new HashMap();
        if (scheduleTaskPeriod != null) {
            map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
        } else {
            return;
        }
        String firstBeginTime = (String) map.get("firstBeginTime");
        String firstOffTime = (String) map.get("firstOffTime");
        int firstResult = Integer.parseInt((String) map.get("firstResult"));
        String secondBeginTime = (String) map.get("secondBeginTime");
        String secondOffTime = (String) map.get("secondOffTime");
        int secondResult = Integer.parseInt((String) map.get("secondResult"));
        String thirdBeginTime = (String) map.get("thirdBeginTime");
        String thirdOffTime = (String) map.get("thirdOffTime");
        int thirdResult = Integer.parseInt((String) map.get("thirdResult"));
        if (endTime.equals(firstOffTime)) {
            firstResult = -1;
        }else if (endTime.equals(secondOffTime)) {
            secondResult = -1;
        }else if (endTime.equals(thirdOffTime)) {
            thirdResult = -1;
        }
        redisCache.setCacheObject("scheduleTaskPeriod", objectMapper.writeValueAsString(map), 60*60*24*30, TimeUnit.SECONDS);
    }

    @PostConstruct
    public void initTime() {
        //初始化时间
        String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
        String firstBeginTime = null;
        String firstOffTime = null;
        Integer firstResult = null;
        String secondBeginTime = null;
        String secondOffTime = null;
        Integer secondResult = null;
        String thirdBeginTime = null;
        String thirdOffTime = null;
        Integer thirdResult = null;
        if (scheduleTaskPeriod != null) {
            try {
                Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
                firstBeginTime = (String) map.get("firstBeginTime");
                firstOffTime = (String) map.get("firstOffTime");
                firstResult = Integer.parseInt((String) map.get("firstResult"));
                secondBeginTime = (String) map.get("secondBeginTime");
                secondOffTime = (String) map.get("secondOffTime");
                secondResult = Integer.parseInt((String) map.get("secondResult"));
                thirdBeginTime = (String) map.get("thirdBeginTime");
                thirdOffTime = (String) map.get("thirdOffTime");
                thirdResult = Integer.parseInt((String) map.get("thirdResult"));
            }catch (JsonProcessingException e) {
                log.error("初始化时间错误："+e.getMessage());
            }
        }
        else {
            firstBeginTime = "2124-12-12 12:00:00";
            firstOffTime = "2124-12-12 13:00:00";
            secondBeginTime = "2124-12-12 12:00:00";
            secondOffTime = "2124-12-12 12:00:00";
            thirdBeginTime = "2124-12-12 12:00:00";
            thirdOffTime = "2124-12-12 12:00:00";
        }
        if (firstResult == null) {
            setTimePeriod("2124-12-12 12:00:00", "2124-12-12 13:00:00");
        } else {
            //0未开始，1进行中，-1已结束
            if (firstResult.equals(1) || (firstResult.equals(0) && secondResult.equals(0) && thirdResult.equals(0))) {
                setTimePeriod(firstBeginTime, firstOffTime);
            }else if (secondResult.equals(1)) {
                setTimePeriod(secondBeginTime, secondOffTime);
            } else if (thirdResult.equals(1)) {
                setTimePeriod(thirdBeginTime, thirdOffTime);
            } else {
                setTimePeriod("2124-12-12 12:00:00", "2124-12-12 13:00:00");
            }
        }
        //初始化登录状态
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        if (userLoginIsRunning != null) {
            redisCache.deleteObject("UserLoginIsRunning");
        }
        LocalDateTime currentTime = LocalDateTime.now();
//        redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);

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
