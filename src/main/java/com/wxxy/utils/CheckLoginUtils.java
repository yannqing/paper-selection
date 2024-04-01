package com.wxxy.utils;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import jakarta.servlet.http.HttpServletRequest;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

public class CheckLoginUtils {

    public static Teacher checkTeacherLoginStatus(HttpServletRequest request) {
        Teacher teacher = (Teacher) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (teacher == null) {
            throw new IllegalStateException("您已退出，请重新登录");
        }
        return teacher;
    }

    public static User checkUserLoginStatus(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new IllegalStateException("您已退出，请重新登录");
        }
        //wqoeiuoi:23     user
        return user;
    }
}
