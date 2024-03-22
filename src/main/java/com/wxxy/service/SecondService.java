package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface SecondService {


    Object login(String username, String password, HttpServletRequest request);

    GetAllByPageVo<Teacher> getFirstTeacherMessage(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) throws JsonProcessingException;

    List<User> getFirstJoinedTeamUsers(HttpServletRequest request, Long teacherId) throws JsonProcessingException;

}
