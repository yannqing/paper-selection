package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.AuthService;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.auth.LoginVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.wxxy.common.UserLoginState.SALT;
import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private TeacherService teacherService;

    @Resource
    private UserService userService;

    @Resource
    private RedisCache redisCache;



    @Override
    public LoginVo login(String username, String password, HttpServletRequest request) throws JsonProcessingException {

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("userAccount",username);
        queryUserWrapper.eq("userPassword",encryptPassword);
        User user = userService.getOne(queryUserWrapper);


        String userLoginIsRunning = redisCache.getCacheObject("UserLoginIsRunning");
        //在时间段内登录
//        if (userLoginIsRunning.equals("true")) {
            //1. 先检测是否是学生登录
            if (user != null && user.getUserStatus() != 1) {
                user.setUserPassword(null);
                //记录用户登录态
                request.getSession().setAttribute(USER_LOGIN_STATE, user);
                if (user.getUserRole() == 1) {
                    log.info("管理员: "+ user.getUsername() +" 登录成功！");
                }
                else {
                    log.info("学生: "+ user.getUsername() +" 登录成功！");
                }
                return new LoginVo<>(user, userLoginIsRunning.equals("true"));
            }
            //2. 检测是否是老师登录，如果也不是老师登录，返回 null
            QueryWrapper<Teacher> queryTeacherWrapper = new QueryWrapper<>();
            queryTeacherWrapper.eq("userAccount",username);
            queryTeacherWrapper.eq("userPassword",encryptPassword);
            Teacher teacher = teacherService.getOne(queryTeacherWrapper);
            if (teacher == null) {
                throw new IllegalStateException("用户名或密码错误，请重试");
            }
            if (teacher.getStatus() == 1) {
                throw new IllegalStateException("您无法正常登录，请联系管理员");
            }
            teacher.setUserPassword(null);
            //记录用户登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, teacher);
            log.info("老师: "+ teacher.getName() +" 登录成功！");
            return new LoginVo<>(teacher, userLoginIsRunning.equals("true"));
//        }
//        else {
//            //非时间段内登录
//            if (user != null && user.getUserRole() == 1) {
//                user.setUserPassword(null);
//                //记录用户登录态
//                request.getSession().setAttribute(USER_LOGIN_STATE, user);
//                log.info("管理员: "+ user.getUsername() +" 登录成功！");
//                return user;
//            }else {
//                throw new IllegalArgumentException("登录失败！不在程序运行时间段内，请联系管理员重试");
//            }
//        }

    }

    @Override
    public void logout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
    }


}
