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
import com.wxxy.utils.RedisCache;
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

    @Resource
    private RedisCache redisCache;

    /**
     * 查询已选择的学生
     * @param request 获取session
     * @return
     */
    @Override
    public List<User> getSelectedStudent(HttpServletRequest request) {
        //获取session中的登录信息
        Teacher teacher = checkTeacherLoginStatus(request, redisCache);
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
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
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
        queryWrapper.eq("teacherId", loginTeacher.getId());
        UserTeam userteam = userTeamService.getOne(queryWrapper);
        if (userteam == null) {
            throw new IllegalArgumentException("学生还未申请，无法加入队伍");
        }
        if (userteam.getIsJoin() == 1) {
            throw new IllegalArgumentException("学生已加入，请勿重复加入");
        }
        Teacher teacher = teacherMapper.selectById(loginTeacher.getId());
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
        //更新老师的队伍数据
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("currentNum", currentNum + 1)      //当前队伍数量+1
                .set("applyNum", applyNum - 1));        //当前申请数量-1
        boolean result = userTeamService.update(updateWrapper);

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<UserTeam>()
                .eq("userId", userId)
                .eq("isJoin", 0);
        UserTeam otherTeam = userTeamService.getOne(userTeamQueryWrapper);

        if (otherTeam != null) {
            //删除此用户的其他申请
            userTeamService.remove(userTeamQueryWrapper);
            //修改与申请相关老师的信息
            Long teacherId = otherTeam.getTeacherId();
            Teacher otherTeacher = teacherMapper.selectById(teacherId);
            Integer otherTeacherApplyNum = otherTeacher.getApplyNum();
            teacherMapper.update(new UpdateWrapper<Teacher>()
                    .eq("id", teacherId)
                    .set("applyNum", otherTeacherApplyNum-1));
        }
        return result;

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
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
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
        queryWrapper.eq("teacherId", loginTeacher.getId());
        UserTeam userteam = userTeamService.getOne(queryWrapper);
        if (userteam == null) {
            throw new IllegalArgumentException("学生还未申请，无法拒绝");
        }
        if (userteam.getIsJoin() == 1) {
            throw new IllegalArgumentException("学生已加入，无法取消申请");
        }
        //取消申请
        int result = userTeamService.getBaseMapper().delete(queryWrapper);
        Teacher teacher = teacherMapper.selectById(loginTeacher.getId());
        Integer applyNum = teacher.getApplyNum();
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", loginTeacher.getId())
                .set("applyNum", applyNum-1));
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
        Teacher teacher = checkTeacherLoginStatus(request, redisCache);
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
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);

        //查询此用户是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teacherId", loginTeacher.getId());
        userTeamQueryWrapper.eq("isJoin", 1);
        UserTeam userJoinedTeam = userTeamService.getOne(userTeamQueryWrapper);
        if (userJoinedTeam == null) {
            throw new IllegalArgumentException("此用户未加入您的队伍，无法进行移出操作");
        }
        //移出队伍
        int result = userTeamService.getBaseMapper().delete(userTeamQueryWrapper);

        Teacher teacher = teacherMapper.selectById(loginTeacher.getId());
        Integer currentNum = teacher.getCurrentNum();
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("currentNum", currentNum - 1));

        return result == 1;
    }

    /**
     * 获取个人信息（学生）
     * @param request 获取session
     * @return
     */
    @Override
    public User getMyselfInfo(HttpServletRequest request) {
        //查看是否登录
        User loginUser = checkUserLoginStatus(request, redisCache);
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

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     * @param againPassword
     * @param request
     * @return
     */
    @Override
    public boolean changeMyPassword(String oldPassword, String newPassword, String againPassword, HttpServletRequest request) {
        //验证登录态
        User loginUser = checkUserLoginStatus(request, redisCache);
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
        User loginUser = checkUserLoginStatus(request, redisCache);
        //更新个人信息
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginUser.getId());
        updateWrapper.set("username", updateUser.getUsername());
        updateWrapper.set("academy", updateUser.getAcademy());
        updateWrapper.set("gender", updateUser.getUserAccount());
        updateWrapper.set("degree", updateUser.getDegree());
        updateWrapper.set("profile", updateUser.getProfile());
        updateWrapper.set("phone", updateUser.getPhone());
        updateWrapper.set("email", updateUser.getEmail());
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = userMapper.update(null, updateWrapper);

        return result == 1;
    }

//    public void test() {
//        methods.add("test");
//    }
}




