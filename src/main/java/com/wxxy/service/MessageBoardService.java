package com.wxxy.service;

import com.wxxy.vo.MessageBoardContentMessage;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface MessageBoardService {
    void studentSendMessage(String message, HttpServletRequest request);

    void teacherSendMessage(String message, HttpServletRequest request);

    List<MessageBoardContentMessage> getMessageBoard(HttpServletRequest request, String symbol);
}