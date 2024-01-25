package com.wxxy.service.impl;
import java.util.ArrayList;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.UserTeam;
import com.wxxy.service.TeacherService;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.UserService;
import com.wxxy.service.UserTeamService;
import com.wxxy.vo.JoinedTeacherStatusVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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
    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectList(null);
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
            //先查询老师的总人数限制
            Teacher teacher = teacherMapper.selectById(teacherId);
            int virtualCount = teacher.getMaxNum();
            //再查询老师队伍的实际人数
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


}




