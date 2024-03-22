package com.wxxy.vo.task;

import org.springframework.stereotype.Component;

@Component
public class SecondEndTask {

    

    public void execute() {
        // 执行任务逻辑
        System.out.println("定时任务执行...");
    }
}