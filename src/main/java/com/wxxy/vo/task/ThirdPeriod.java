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

        // 判断当前时间是否在指定的时间段内 TODO 判空 startTIme，endTime
        if (startTime == null || endTime == null) {
            initTime();
        }
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))) {
            // 在时间段内
            log.info("--------在任务执行的时间段内，任务持续执行--------");
            isExecuteInit = false;
            redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
                execute();
        } else {
            // 不在时间段内
            log.info("--------不在任务时间段内，持续跟踪--------");
            if (userLoginIsRunning.equals("true")) {
                redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
            }
            if (currentTime.isAfter(endTime) && !isExecuteInit) {
                //任务执行结束，重新给结果赋值
                log.info("上一任务时间段结束，准备完成任务！");
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
        if (endTime.equals(LocalDateTime.parse(firstOffTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))) {
            log.info("第一阶段任务结束，自动修改状态为已结束");
            firstResult = -1;
        }else if (endTime.equals(LocalDateTime.parse(secondOffTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))) {
            log.info("第二阶段任务结束，自动修改状态为已结束");
            secondResult = -1;
        }else if (endTime.equals(LocalDateTime.parse(thirdOffTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))) {
            log.info("第三阶段任务结束，自动修改状态为已结束");
            thirdResult = -1;
        }
        map.remove("firstResult");
        map.remove("secondResult");
        map.remove("thirdResult");
        map.put("firstResult", firstResult);
        map.put("secondResult", secondResult);
        map.put("thirdResult", thirdResult);
        redisCache.setCacheObject("scheduleTaskPeriod", objectMapper.writeValueAsString(map), 60*60*24*30, TimeUnit.SECONDS);
    }

    @PostConstruct
    public void initTime() {
        try {
            String scheduleTaskPeriod = redisCache.getCacheObject("scheduleTaskPeriod");
            if (scheduleTaskPeriod != null) {
                Map map = objectMapper.readValue(scheduleTaskPeriod, Map.class);
                String firstBeginTime = (String) map.getOrDefault("firstBeginTime", "2124-12-12 12:00:00");
                String firstOffTime = (String) map.getOrDefault("firstOffTime", "2124-12-12 13:00:00");
                Integer firstResult = parseIntegerSafe((String) map.get("firstResult"));
                String secondBeginTime = (String) map.getOrDefault("secondBeginTime", "2124-12-12 12:00:00");
                String secondOffTime = (String) map.getOrDefault("secondOffTime", "2124-12-12 12:00:00");
                Integer secondResult = parseIntegerSafe((String) map.get("secondResult"));
                String thirdBeginTime = (String) map.getOrDefault("thirdBeginTime", "2124-12-12 12:00:00");
                String thirdOffTime = (String) map.getOrDefault("thirdOffTime", "2124-12-12 12:00:00");
                Integer thirdResult = parseIntegerSafe((String) map.get("thirdResult"));

                resolveTimePeriod(firstResult, secondResult, thirdResult, firstBeginTime, firstOffTime, secondBeginTime, secondOffTime, thirdBeginTime, thirdOffTime);
            } else {
                log.info("管理员未设置时间段，默认初始化时间：{2124-12-12 12:00:00, 2124-12-12 13:00:00}");
                setTimePeriod("2124-12-12 12:00:00", "2124-12-12 13:00:00");
            }

            manageUserLoginStatus();

        } catch (Exception e) {
            log.error("初始化时间错误：" + e.getMessage(), e);
        }
    }

    private Integer parseIntegerSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void manageUserLoginStatus() {
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        if (userLoginIsRunning != null) {
            redisCache.deleteObject("UserLoginIsRunning");
        }
        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))) {
            redisCache.setCacheObject("UserLoginIsRunning", "true", 60*60*24*30, TimeUnit.SECONDS);
        } else {
            redisCache.setCacheObject("UserLoginIsRunning", "false", 60*60*24*30, TimeUnit.SECONDS);
        }
    }

    private void resolveTimePeriod(Integer firstResult, Integer secondResult, Integer thirdResult,
                                   String firstBeginTime, String firstOffTime,
                                   String secondBeginTime, String secondOffTime,
                                   String thirdBeginTime, String thirdOffTime) {
        if (firstResult != null && (firstResult.equals(1) || (firstResult.equals(0) && secondResult.equals(0) && thirdResult.equals(0)))) {
            log.info("当前处于第一阶段，开始时间：{}, 结束时间：{}", firstBeginTime, firstOffTime);
            setTimePeriod(firstBeginTime, firstOffTime);
        } else if (secondResult != null && secondResult.equals(1)) {
            log.info("当前处于第二阶段，开始时间：{}, 结束时间：{}", secondBeginTime, secondOffTime);
            setTimePeriod(secondBeginTime, secondOffTime);
        } else if (thirdResult != null && thirdResult.equals(1)) {
            log.info("当前处于第三阶段，开始时间：{}, 结束时间：{}", thirdBeginTime, thirdOffTime);
            setTimePeriod(thirdBeginTime, thirdOffTime);
        } else {
            log.info("当前不处于任何阶段，默认开始时间：{2124-12-12 12:00:00}, 默认结束时间：{2124-12-12 13:00:00}");
            setTimePeriod("2124-12-12 12:00:00", "2124-12-12 13:00:00");  // Default time period
        }
    }
    public void execute() {
        log.info("任务3执行");
    }
}
