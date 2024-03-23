package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface ScheduledTaskService {

    public void scheduleTask(String firstTime, String secondTime, String thirdTime);

    void scheduleTask(String firstBeginTime, String firstEndTime, String secondBeginTime, String secondEndTime, String thirdBeginTime, String thirdEndTime, HttpServletRequest request) throws JsonProcessingException;

    Map getScheduleTasks(HttpServletRequest request) throws JsonProcessingException;
}
