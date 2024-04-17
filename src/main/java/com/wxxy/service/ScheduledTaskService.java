package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface ScheduledTaskService {

    void scheduleTask(String firstTime, String secondTime, String thirdTime);

    void scheduleTask(String firstBeginTime,
                      String firstOffTime,
                      Integer firstResult,
                      String secondBeginTime,
                      String secondOffTime,
                      Integer secondResult,
                      String thirdBeginTime,
                      String thirdOffTime,
                      Integer thirdResult,
                      HttpServletRequest request) throws JsonProcessingException;

    Map getScheduleTasks(HttpServletRequest request) throws JsonProcessingException;

    boolean forbidden(HttpServletRequest request);

    boolean updateSize(HttpServletRequest request);
}
