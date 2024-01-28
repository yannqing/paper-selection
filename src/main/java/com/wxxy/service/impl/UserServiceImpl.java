package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.mapper.UserMapper;
import com.wxxy.service.UserTeamService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-01-22 01:11:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserTeamService userTeamService;

    /**
     * 查询已选择的学生
     * @param request 获取session
     * @return
     */
    @Override
    public List<User> getSelectedStudent(HttpServletRequest request) {
        //获取session中的登录信息
        Teacher teacher = (Teacher) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);
        if (teacher == null) {
            throw new RuntimeException("您已退出，请重新登录");
        }
        //查询已选择老师的所有学生Id
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacher.getId());
        List<UserTeam> userTeams = userTeamService.getBaseMapper().selectList(queryWrapper);
        if (userTeams.size() == 0) {
            return null;
        }

        List<User> selectedUser = new ArrayList<>();
        for (UserTeam userTeam : userTeams) {
            User user = this.getById(userTeam.getUserId());
            selectedUser.add(user);
        }

        return selectedUser;
    }

    /**
     * 同意学生加入队伍
     * @param userId 学生id
     * @param request 获取老师id
     * @return
     */
    @Override
    public boolean agreeJoin(Long userId, HttpServletRequest request) {
        //查询是否登录
        Teacher teacher = (Teacher) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);
        if (teacher == null) {
            throw new RuntimeException("您已退出，请重新登录");
        }
        //查询用户id是否合法
        if (userId == null) {
            throw new IllegalArgumentException("用户id为空");
        }
        if (this.getById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        //查询用户是否已加入，是否已申请
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teacherId", teacher.getId());
        UserTeam userteam = userTeamService.getOne(queryWrapper);
        if (userteam == null) {
            throw new IllegalArgumentException("学生还未申请，无法加入队伍");
        }
        if (userteam.getIsJoin() == 1) {
            throw new IllegalArgumentException("学生已加入，请勿重复加入");
        }
        //用户加入队伍
        UpdateWrapper<UserTeam> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("teacherId", teacher.getId());
        updateWrapper.set("isJoin", 1);
        return userTeamService.update(null, updateWrapper);

    }


}




