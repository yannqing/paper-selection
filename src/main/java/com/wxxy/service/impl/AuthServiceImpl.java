package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.AuthService;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private TeacherService teacherService;

    @Resource
    private UserService userService;

    public static final String SALT = "paperSelection";
    public static final String USER_LOGIN_STATE = "userLoginState";

    @Override
    public <T> Object login(String username, String password, HttpServletRequest request) {

        //1. 先检测是否是学生登录

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("userAccount",username);
        queryUserWrapper.eq("userPassword",encryptPassword);
        User user = userService.getOne(queryUserWrapper);
        if (user != null) {
            user.setUserPassword(null);
            //记录用户登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            return user;
        }
        //检测是否是老师登录，如果也不是老师登录，返回null
        QueryWrapper<Teacher> queryTeacherWrapper = new QueryWrapper<>();
        queryTeacherWrapper.eq("userAccount",username);
        queryTeacherWrapper.eq("userPassword",encryptPassword);
        Teacher teacher = teacherService.getOne(queryTeacherWrapper);
        teacher.setUserPassword(null);
        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, teacher);
        return teacher;

    }
}
