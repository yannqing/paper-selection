package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.service.MessageBoardService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.MessageBoardContentMessageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "留言板")
@RestController
@RequestMapping("/message-board")
public class MessageBoardController {

    @Resource
    private MessageBoardService messageBoardService;

    @Operation(summary = "学生发送消息到留言板")
    @PostMapping("/student/send")
    public BaseResponse<Object> studentSendMessage (String message, HttpServletRequest request) {
        messageBoardService.studentSendMessage(message, request);
        return ResultUtils.success();
    }

    @Operation(summary = "老师发送消息到留言板")
    @PostMapping("/teacher/send")
    public BaseResponse<Object> teacherSendMessage (String message, HttpServletRequest request) {
        messageBoardService.teacherSendMessage(message, request);
        return ResultUtils.success();
    }

    @Operation(summary = "获取留言板内容")
    @Parameters({@Parameter(name = "symbol", description = "传入标识：1 是老师，0 是学生")})
    @GetMapping("/get/messageBoard")
    public BaseResponse<List<MessageBoardContentMessageVo>> getMessageBoard (HttpServletRequest request, String symbol) {
        List<MessageBoardContentMessageVo> messageContent = messageBoardService.getMessageBoard(request, symbol);
        return ResultUtils.failure(Code.SUCCESS, messageContent, "查询留言板消息成功！");
    }
}
