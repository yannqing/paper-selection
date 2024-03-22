package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.SecondService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import static com.wxxy.common.UserLoginState.SALT;
import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

@Service
@Slf4j
public class SecondServiceImpl implements SecondService {

    @Resource
    private UserTeamMapper userTeamMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserMapper userMapper;


    @Override
    public Object login(String username, String password, HttpServletRequest request) {
        //1. 先检测是否是学生登录
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("userAccount",username);
        queryUserWrapper.eq("userPassword",encryptPassword);
        queryUserWrapper.eq("userStatus", 0);
        User user = userMapper.selectOne(queryUserWrapper);
        if (user != null) {
            user.setUserPassword(null);
            //记录用户登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            log.info("用户: "+ user.getUsername() +" 登录成功！");
            return user;
        }
        //2. 检测是否是老师登录，如果也不是老师登录，返回 null
        QueryWrapper<Teacher> queryTeacherWrapper = new QueryWrapper<>();
        queryTeacherWrapper.eq("userAccount",username);
        queryTeacherWrapper.eq("userPassword",encryptPassword);
        Teacher teacher = teacherMapper.selectOne(queryTeacherWrapper);
        if (teacher == null ) {
            throw new IllegalStateException("用户名或密码错误，请重试");
        } else if (teacher.getMaxNum() == 0) {
            throw new IllegalArgumentException("您队伍已满，无法参加本轮筛选，请联系管理员重试");
        }
        teacher.setUserPassword(null);
        //记录用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, teacher);
        log.info("老师: "+ teacher.getName() +" 登录成功！");
        return teacher;
    }
}
