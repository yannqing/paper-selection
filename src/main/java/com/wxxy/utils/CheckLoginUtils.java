package com.wxxy.utils;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

public class CheckLoginUtils {

    public static final String[] whiteStudentURL = {
            "/student/updateMyselfInfo",
            "/student/changeMyPassword",
            "/student/getMyselfInfo",
            "/student/getJoinedTeacherStatus",
            "/student/getAll",
            "/student/getAccount",
            "/message-board/student/send",
            "/message-board/get/messageBoard",
    };
    public static final String[] whiteTeacherURL = {
            "/teacher/uploadAvatar",
            "/teacher/updateMyselfInfo",
            "/teacher/changeMyPassword",
            "/teacher/changeMaxSize",
            "/teacher/changeApplySize",
            "/teacher/getMyselfInfo",
            "/teacher/getMySelectedStudent",
            "/teacher/getMyJoinedStudent",
            "/teacher/getCountOfTeam",
            "/message-board/teacher/send",
            "/message-board/get/messageBoard",
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
            if (user == null) {
                throw new IllegalStateException("您的登录已失效，请重新登录");
            }
            //普通用户，且访问的是黑名单的路径，则抛异常
            if (user.getUserRole() == 0 && !whiteStudents.contains(request.getRequestURI())) {
                throw new IllegalArgumentException("您未授权，请重试");
            }
        }
        return user;
    }
}
