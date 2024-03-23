package com.wxxy.vo.task;

import com.wxxy.common.UserLoginState;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ThirdPeriod {

    private static LocalDateTime startTime; // 开始时间
    private static LocalDateTime endTime;   // 结束时间
    private boolean isExecute = false;

    // 设置开始时间和结束时间
    public static void setTimePeriod(String start, String end) {
        startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Scheduled(cron = "0 * * * * ?")
    public void SecondTask() {
        // 获取当前时间
        LocalDateTime currentTime = LocalDateTime.now();

        // 判断当前时间是否在指定的时间段内
        if (currentTime.isAfter(startTime.minusSeconds(1)) && currentTime.isBefore(endTime.plusSeconds(1))) {
            // 在时间段内，解开登录状态
            UserLoginState.isRunning = true;
            execute();
        } else {
            // 不在时间段内
            UserLoginState.isRunning = false;
            if (currentTime.isBefore(startTime.minusHours(1)) && !isExecute) {
                init();
                isExecute = true;
            }
        }
    }

    public void init() {
        System.out.println("任务3初始化操作！");
    }

    @PostConstruct
    public void initTime() {
        setTimePeriod("2124-12-12 12:00:00", "2124-12-12 13:00:00");
    }


    public void execute() {
        System.out.println("任务3执行");
    }
}
