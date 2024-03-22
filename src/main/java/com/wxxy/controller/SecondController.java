package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.SecondService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.GetAllByPageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/second")
public class SecondController {

    @Resource
    private SecondService secondService;


    /**
     * 登录
     * @param username
     * @param password
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<Object> login(String username, String password, HttpServletRequest request){
        Object login = secondService.login(username, password, request);
        return ResultUtils.success(Code.LOGIN_SUCCESS, login, "登录成功");
    }

    /**
     * 查询第一轮的老师信息
     * @param currentPage
     * @param pageSize
     * @param searchAccount
     * @param request
     * @return
     */
    @GetMapping("/getFirstTeacherMessage")
    public BaseResponse<GetAllByPageVo<Teacher>> getFirstTeacherMessage(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) throws JsonProcessingException {
        GetAllByPageVo<Teacher> firstTeacherMessage = secondService.getFirstTeacherMessage(currentPage, pageSize, searchAccount, request);
        return ResultUtils.success(Code.SUCCESS, firstTeacherMessage, "查询第一轮的老师信息成功");
    }

    /**
     * 查询第一轮中，老师队伍的成员
     * @param request
     * @param teacherId
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping("/getFirstTeacherTeam")
    public BaseResponse<List<User>> getFirstJoinedTeamUsers(HttpServletRequest request, Long teacherId) throws JsonProcessingException {
        List<User> firstJoinedTeamUsers = secondService.getFirstJoinedTeamUsers(request, teacherId);
        return ResultUtils.success(Code.SUCCESS, firstJoinedTeamUsers, "查询第一轮老师的队伍成员成功");
    }




}
