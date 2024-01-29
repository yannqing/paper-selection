package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.mapper.UserMapper;
import com.wxxy.service.UserTeamService;
import com.wxxy.vo.BaseResponse;
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

    @Resource
    private TeacherMapper teacherMapper;

    /**
     * 查询已选择的学生
     * @param request 获取session
     * @return
     */
    @Override
    public List<User> getSelectedStudent(HttpServletRequest request) {
        //获取session中的登录信息
        Teacher teacher = checkLoginStatus(request);
        //查询已选择老师的所有学生Id
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacher.getId());
        queryWrapper.eq("isJoin", 0);
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
        Teacher teacher = checkLoginStatus(request);
        //查询用户id是否合法
        if (userId == null) {
            throw new IllegalArgumentException("用户id为空");
        }
        if (this.getById(userId) == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        //查询队伍数量是否已达最大
        QueryWrapper<UserTeam> queryUserTeamWrapper = new QueryWrapper<>();
        queryUserTeamWrapper.eq("teacherId", teacher.getId());
        List<UserTeam> userTeams = userTeamService.getBaseMapper().selectList(queryUserTeamWrapper);
        if (userTeams.size() >= teacher.getMaxNum()) {
            throw new IllegalArgumentException("您的队伍已经达最大数量，无法同意加入");
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

    /**
     * 查看我的队伍
     * @param request
     * @return
     */
    @Override
    public List<User> joinedStudent(HttpServletRequest request) {
        //获取老师id
        Teacher teacher = checkLoginStatus(request);
        //查询我的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacher.getId());
        queryWrapper.eq("isJoin", 1);
        List<UserTeam> myTeams = userTeamService.getBaseMapper().selectList(queryWrapper);
        List<User> joinedUsers = new ArrayList<>();
        for (UserTeam userTeam : myTeams) {
            QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
            queryUserWrapper.eq("id", userTeam.getUserId());
            User joinedUser = this.getOne(queryUserWrapper);
            //脱敏
            joinedUser.setUserPassword(null);
            joinedUsers.add(joinedUser);
        }

        return joinedUsers;
    }

    /**
     * 移出队伍
     * @param userId 要移出的用户id
     * @param request
     * @return
     */
    @Override
    public boolean removeFromTeam(Long userId, HttpServletRequest request) {
        //校验用户id是否合法
        if (userId == null) {
            throw new IllegalArgumentException("用户id不能为空");
        }
        if (this.getById(userId) == null) {
            throw new IllegalStateException("此用户不存在");
        }

        //查询是否登录
        Teacher teacher = checkLoginStatus(request);
        //查询此用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teacherId", teacher.getId());
        userTeamQueryWrapper.eq("isJoin", 1);
        UserTeam userJoinedTeam = userTeamService.getOne(userTeamQueryWrapper);
        if (userJoinedTeam == null) {
            throw new IllegalStateException("此用户未加入您的队伍，无法进行移出操作");
        }
        //移出队伍
        int result = userTeamService.getBaseMapper().delete(userTeamQueryWrapper);
        return result == 1;
    }

    /**
     * 更改队伍容量
     * @param maxSize 要修改的容量
     * @param request 获取老师信息
     * @return
     */
    @Override
    public boolean changeMaxSize(int maxSize, HttpServletRequest request) {
        //参数校验
        if (maxSize < 0) {
            throw new IllegalArgumentException("队伍最大数量不能小于0");
        }
        //查询是否登录
        Teacher teacher = checkLoginStatus(request);
        //查寻要修改的数量是否小于队伍中已存在的用户数量
        QueryWrapper<UserTeam> queryUserTeamWrapper = new QueryWrapper<>();
        queryUserTeamWrapper.eq("teacherId", teacher.getId());
        queryUserTeamWrapper.eq("isJoin", 1);
        List<UserTeam> joinedUser = userTeamService.getBaseMapper().selectList(queryUserTeamWrapper);
        if (joinedUser.size() > maxSize) {
            throw new IllegalArgumentException("要修改的数量不能低于队伍中已有的成员数量，若要修改，请先移出部分成员");
        }
        //修改最大数量
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teacher.getId());
        updateWrapper.set("maxNum", maxSize);
        int result = teacherMapper.update(updateWrapper);
        return result == 1;
    }



    public Teacher checkLoginStatus(HttpServletRequest request) {
        Teacher teacher = (Teacher) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);
        if (teacher == null) {
            throw new RuntimeException("您已退出，请重新登录");
        }
        return teacher;
    }
}




