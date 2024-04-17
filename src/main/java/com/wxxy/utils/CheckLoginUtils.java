package com.wxxy.utils;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

public class CheckLoginUtils {

    public static final String[] whiteStudentURL = {
            "/teacher/getAll",
            "/teacher/getAccount",
            "/teacher/getJoinedTeacherStatus",
            "/teacher/uploadAvatar",
            "/student/getMyselfInfo",
            "/student/changeMyPassword",
            "/student/updateMyselfInfo",
    };
    public static final String[] whiteTeacherURL = {
            "/teacher/getCountOfTeam",
            "/teacher/getMyselfInfo",
            "/teacher/changeMyPassword",
            "/teacher/updateMyselfInfo",
            "/student/getMySelectedStudent",
            "/student/getMyJoinedStudent",
            "/student/getMySelectedStudent",
            "/student/getMySelectedStudent",
            "/student/getMySelectedStudent",
            "/student/getMySelectedStudent",
            "/student/getMySelectedStudent"
    };

    public static final List<String> whiteStudents = Arrays.asList(whiteStudentURL);
    public static final List<String> whiteTeachers = Arrays.asList(whiteTeacherURL);

    public static Teacher checkTeacherLoginStatus(HttpServletRequest request, RedisCache redisCache) {
        Teacher teacher = (Teacher) request.getSession().getAttribute(USER_LOGIN_STATE);
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

        if (teacher == null || userLoginIsRunning.equals("false")) {
            if (!whiteTeachers.contains(request.getRequestURI())) {
                throw new IllegalArgumentException("您未授权，请重试");
            }
        }
        return teacher;
    }

    public static User checkUserLoginStatus(HttpServletRequest request, RedisCache redisCache) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        //不在登录时间
        if (userLoginIsRunning.equals("false") || user == null) {
            //普通用户
            assert user != null;
            //普通用户，且访问的是黑名单的路径，则抛异常
            if (user.getUserRole() == 0 && !whiteStudents.contains(request.getRequestURI())) {
                throw new IllegalArgumentException("您未授权，请重试");
            }
        }
        return user;
    }
}
