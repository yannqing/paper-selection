package com.wxxy.utils;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Component;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

public class CheckLoginUtils {

    public static Teacher checkTeacherLoginStatus(HttpServletRequest request, RedisCache redisCache) {
        Teacher teacher = (Teacher) request.getSession().getAttribute(USER_LOGIN_STATE);
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

        if (teacher == null || userLoginIsRunning.equals("false")) {
            throw new IllegalStateException("您已退出，请重新登录");
        }
        return teacher;
    }

    public static User checkUserLoginStatus(HttpServletRequest request, RedisCache redisCache) {
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");

        if (user == null || userLoginIsRunning.equals("false")) {
            assert user != null;
            //普通用户
            if (user.getUserRole() == 0) {
                throw new IllegalStateException("您已退出，请重新登录");
            }
        }
        return user;
    }
}
