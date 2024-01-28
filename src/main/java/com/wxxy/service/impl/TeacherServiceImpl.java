package com.wxxy.service.impl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.service.TeacherService;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.UserService;
import com.wxxy.service.UserTeamService;
import com.wxxy.vo.JoinedTeacherStatusVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author 67121
* @description 针对表【teacher(老师(队伍))】的数据库操作Service实现
* @createDate 2024-01-24 00:24:30
*/
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>

    implements TeacherService{

    @Resource
    private UserService userService;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamService userTeamService;

    @Override
    public List<StudentGetTeachersVo> getAllTeachers() {
        List<Teacher> teachers = teacherMapper.selectList(null);
        List<StudentGetTeachersVo> studentGetTeachersVos = new ArrayList<>();
        for (Teacher teacher : teachers) {
            StudentGetTeachersVo studentGetTeachersVo = new StudentGetTeachersVo();
            studentGetTeachersVo.setTeacherId(teacher.getId());
            studentGetTeachersVo.setTeacherName(teacher.getName());
            studentGetTeachersVo.setTeacherDescription(teacher.getDescription());
            studentGetTeachersVo.setEmail(teacher.getEmail());
            studentGetTeachersVo.setPhone(teacher.getPhone());
            studentGetTeachersVo.setAvatarUrl(teacher.getAvatarUrl());
            studentGetTeachersVo.setMaxNum(teacher.getMaxNum());
            //加入数量，剩余数量
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teacherId", teacher.getId());
            userTeamQueryWrapper.eq("isJoin", 1);
            List<UserTeam> userTeams = userTeamService.getBaseMapper().selectList(userTeamQueryWrapper);
            studentGetTeachersVo.setJoinedNum(userTeams.size());
            studentGetTeachersVo.setRemainingNum(teacher.getMaxNum() - userTeams.size());
            studentGetTeachersVos.add(studentGetTeachersVo);
        }
        return studentGetTeachersVos;
    }


    //1. 重复申请/加入
    @Override
    public boolean joinTeacher(int[] teacherIds, Long userId) {
        //选择的老师数量不能超过2
        if (teacherIds.length > 2) {
            throw new IllegalArgumentException("选择老师的数量过多");
        }
        //查询{已选择的老师数量}是否为2（达到最大）
        int teacherAccount = this.selectedTeacherAccount(userId);
        if(2 - teacherAccount - teacherIds.length < 0) {
            throw new IllegalArgumentException("选择的老师数量超过可选择的范围！");
        }
        //查询对应老师的队伍人数限制是否达到最大
        for (int teacherId : teacherIds) {
            //1. 先查询老师的总人数限制
            Teacher teacher = teacherMapper.selectById(teacherId);
            int virtualCount = teacher.getMaxNum();
            //2. 再查询老师队伍的实际人数
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teacherId", teacherId);
            List<UserTeam> userTeams = userTeamService.getBaseMapper().selectList(queryWrapper);
            int actualCount = userTeams.size();
            if (actualCount >= virtualCount) {
                throw new IllegalStateException("老师队伍已满，无法申请");
            }
            else if(userTeamService.getOne(new QueryWrapper<UserTeam>().eq("teacherId",teacherId).eq("userId",userId))!=null){
                throw new IllegalStateException("已经申请加入此老师的队伍，请勿重复加入:"+teacherId);
            }
            else {
                //加入老师队伍
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeacherId((long) teacherId);
                userTeamService.getBaseMapper().insert(userTeam);
            }
        }
        return true;
        //加入老师队伍
    }

    /**
     * 查询该用户选择的老师队伍数量
     * @param userId
     * @return
     */
    @Override
    public int selectedTeacherAccount(Long userId) {
        //查询用户Id是否合法
        if (userService.getById(userId) == null) {
            throw new IllegalStateException("此用户id不存在");
        }
        //查询此用户申请的队伍数量
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);

        List<UserTeam> selectedAccount = userTeamService.getBaseMapper().selectList(queryWrapper);

        return selectedAccount.size();
    }

    /**
     * 返回用户加入的所有队伍名称和状态
     * @param userId
     * @return
     */
    @Override
    public List<JoinedTeacherStatusVo> getJoinedTeacherStatus(Long userId) {
        //查询用户Id是否合法
        if (userService.getById(userId) == null) {
            throw new IllegalStateException("此用户id不存在");
        }

        List<JoinedTeacherStatusVo> joinedTeacherStatus = new ArrayList<>();
        //查询userTeam表中的用户加入的队伍id
        List<UserTeam> userJoinedTeams = userTeamService.getBaseMapper().selectList(new QueryWrapper<UserTeam>().eq("userId", userId));
        //循环申请的每一个队伍
        for (UserTeam userJoinedTeam : userJoinedTeams) {
            JoinedTeacherStatusVo joinedTeacherStatusVo = new JoinedTeacherStatusVo();
            //加入的状态
            if (userJoinedTeam.getIsJoin() == 0) {
                joinedTeacherStatusVo.setStatus("审核中");
            } else {
                joinedTeacherStatusVo.setStatus("已加入");
            }
            joinedTeacherStatusVo.setTeacherId(userJoinedTeam.getTeacherId());
            Teacher teacher = this.getById(userJoinedTeam.getTeacherId());
            joinedTeacherStatusVo.setTeacherName(teacher.getName());
            joinedTeacherStatus.add(joinedTeacherStatusVo);
        }
        return joinedTeacherStatus;
    }

    @Override
    public boolean uploadAvatar(MultipartFile avatar, HttpServletRequest request) throws IOException {

        byte[] avatarBytes = avatar.getBytes();
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        Teacher teacher = (Teacher) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);
        updateWrapper.eq("id",teacher.getId());
        updateWrapper.set("avatarUrl", avatarBytes);
        teacherMapper.update(null, updateWrapper);
        return false;
    }

    /**
     * 退出队伍
     * @param teacherId 要退出的队伍id
     * @param request
     * @return
     */
    @Override
    public boolean exitTeam(Long teacherId, HttpServletRequest request) {
        //1. 检查teacherId是否合法
        if (teacherId == null) {
            throw new IllegalArgumentException("老师Id不能为空");
        }
        if (this.getById(teacherId) == null) {
            throw new IllegalArgumentException("老师不存在");
        }

        //获取到此用户的信息
        User loginUser = (User) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);

        //退出队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.eq("userId", loginUser.getId());
        //判断用户是否加入此队伍
        if (userTeamService.getOne(queryWrapper) == null) {
            throw new IllegalStateException("用户: "+loginUser.getUsername()+" 并未加入此队伍teacherId: "+teacherId+"，退出失败");
        }
        int deleteResult = userTeamService.getBaseMapper().delete(queryWrapper);

        return deleteResult == 1;
    }


}




