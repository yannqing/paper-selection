package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.common.DateFormat;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.mapper.UserMapper;
import com.wxxy.service.UserTeamService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.UserVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.wxxy.common.UserLoginState.SALT;
import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;
import static com.wxxy.utils.CheckLoginUtils.checkTeacherLoginStatus;
import static com.wxxy.utils.CheckLoginUtils.checkUserLoginStatus;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-01-22 01:11:58
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserMapper userMapper;

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
        Teacher teacher = checkTeacherLoginStatus(request);
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
        Teacher teacher = checkTeacherLoginStatus(request);
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
        //查询队伍数量是否已达最大
        Integer currentNum = teacher.getCurrentNum();
        Integer applyNum = teacher.getApplyNum();
        if (currentNum >= teacher.getMaxNum()) {
            throw new IllegalArgumentException("您的队伍已经达最大数量，无法同意加入");
        }


        //用户加入队伍
        UpdateWrapper<UserTeam> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("teacherId", teacher.getId());
        updateWrapper.set("isJoin", 1);
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("currentNum", currentNum + 1)      //当前队伍数量+1
                .set("applyNum", applyNum - 1));        //当前申请数量-1
        return userTeamService.update(null, updateWrapper);

    }

    /**
     * 拒绝学生加入
     * @param userId
     * @param request
     * @return
     */
    @Override
    public boolean disagreeJoin(Long userId, HttpServletRequest request) {
        //查询是否登录
        Teacher teacher = checkTeacherLoginStatus(request);
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
            throw new IllegalArgumentException("学生还未申请，无法拒绝");
        }
        if (userteam.getIsJoin() == 1) {
            throw new IllegalArgumentException("学生已加入，无法取消申请");
        }
        //取消申请
        int result = userTeamService.getBaseMapper().delete(queryWrapper);
        return result == 1;
    }


    /**
     * 查看我的队伍
     * @param request
     * @return
     */
    @Override
    public List<User> joinedStudent(HttpServletRequest request) {
        //获取老师id
        Teacher teacher = checkTeacherLoginStatus(request);
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
            throw new IllegalArgumentException("此用户不存在");
        }

        //查询是否登录
        Teacher teacher = checkTeacherLoginStatus(request);
        Integer currentNum = teacher.getCurrentNum();
        //查询此用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teacherId", teacher.getId());
        userTeamQueryWrapper.eq("isJoin", 1);
        UserTeam userJoinedTeam = userTeamService.getOne(userTeamQueryWrapper);
        if (userJoinedTeam == null) {
            throw new IllegalArgumentException("此用户未加入您的队伍，无法进行移出操作");
        }
        //移出队伍
        int result = userTeamService.getBaseMapper().delete(userTeamQueryWrapper);

        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("currentNum", currentNum - 1));

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
        Teacher teacher = checkTeacherLoginStatus(request);
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
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = teacherMapper.update(updateWrapper);
        return result == 1;
    }

    /**
     * 更改申请容量
     * @param applySize 最新申请限制数量
     * @param request 验证登录
     * @return
     */
    @Override
    public boolean changeApplySize(int applySize, HttpServletRequest request) {
        //查询参数是否合法
        if (applySize <= 0) {
            throw new IllegalArgumentException("参数不合法，申请容量不能<=0");
        }
        //查询是否登录
        Teacher loginTeacher = checkTeacherLoginStatus(request);
        //查询修改的容量是否小于已经申请的容量
        Teacher teacher = teacherMapper.selectById(loginTeacher.getId());
        if (teacher.getApplyNum() >= applySize) {
            throw new IllegalArgumentException("申请限制不能小于已申请数量！");
        }
        //修改申请容量
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("maxApply", applySize)
                .set("updateTime", DateFormat.getCurrentTime()));

        return true;
    }

    /**
     * 获取个人信息（学生）
     * @param request 获取session
     * @return
     */
    @Override
    public User getMyselfInfo(HttpServletRequest request) {
        //查看是否登录
        User loginUser = checkUserLoginStatus(request);
        //获取个人信息
        User userMsg = this.getBaseMapper().selectOne(new QueryWrapper<User>().eq("id", loginUser.getId()));
        //脱敏
//        UserVo userVo = new UserVo();
//        userVo.setId(userMsg.getId());
//        userVo.setUsername(userMsg.getUsername());
//        userVo.setAcademy(userMsg.getAcademy());
//        userVo.setDegree(userMsg.getDegree());
//        userVo.setUserAccount(userMsg.getUserAccount());
//        userVo.setGender(userMsg.getGender());
//        userVo.setProfile(userMsg.getProfile());
//        userVo.setPhone(userMsg.getPhone());
//        userVo.setEmail(userMsg.getEmail());

        userMsg.setUserPassword(null);

        return userMsg;
    }

    @Override
    public boolean changeMyPassword(String oldPassword, String newPassword, String againPassword, HttpServletRequest request) {
        //验证登录态
        User loginUser = checkUserLoginStatus(request);
        User user = userMapper.selectById(loginUser.getId());
        //确保两次输入密码相同
        if (!Objects.equals(newPassword, againPassword)) {
            throw new IllegalArgumentException("两次输入密码不同，请重试！");
        }
        String oldEncryptPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        //判断原密码是否相同
        if (!oldEncryptPassword.equals(user.getUserPassword())) {
            throw new IllegalArgumentException("原密码错误，请重试！");
        }
        //修改密码
        int result = userMapper.update(new UpdateWrapper<User>()
                .eq("id", user.getId())
                .set("userPassword", newEncryptPassword)
                .set("updateTime", DateFormat.getCurrentTime()));

        //移出登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        log.info("学生修改密码成功，请重新登录！");

        return result == 1;
    }

    /**
     * 修改个人信息（学生）
     * @param updateUser
     * @param request
     * @return
     */
    @Override
    public boolean updateMyselfInfo(User updateUser, HttpServletRequest request) {
        //校验登录态
        User loginUser = checkUserLoginStatus(request);
        //更新个人信息
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginUser.getId());
        updateWrapper.set("username", updateUser.getUsername());
        updateWrapper.set("academy", updateUser.getAcademy());
        updateWrapper.set("degree", updateUser.getDegree());
        updateWrapper.set("profile", updateUser.getProfile());
        updateWrapper.set("phone", updateUser.getPhone());
        updateWrapper.set("email", updateUser.getEmail());
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = userMapper.update(null, updateWrapper);

        return result == 1;
    }


}




