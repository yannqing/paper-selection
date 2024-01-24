package com.wxxy.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.UserTeam;
import com.wxxy.service.TeacherService;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.UserTeamService;
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
    TeacherMapper teacherMapper;

    @Resource
    UserTeamService userTeamService;

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectList(null);
    }



    @Override
    public boolean joinTeacher(int[] teacherIds, Long userId) {
        //选择的老师数量不能超过2
        if (teacherIds.length > 2) {
            throw new IllegalArgumentException("选择老师的数量过多");
        }
        //查询已选择的老师数量是否为2（达到最大）
        int teacherAccount = this.selectedTeacherAccount(userId);
        if (teacherAccount >= 2) {
            throw new IllegalArgumentException("选择的老师数量已达最大，不能继续进行选择");
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
                throw new IllegalStateException("老师队伍已满，无法选择");
            }
            else {
                //加入老师队伍
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeacherId((long) teacherId);
                userTeamService.getBaseMapper().insert(userTeam);

                return true;
            }
        }

        //加入老师队伍
    }

    /**
     * 查询该用户选择的老师队伍数量
     * @param userId
     * @return
     */
    @Override
    public int selectedTeacherAccount(Long userId) {
        //查询用户Id是否存在
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);

        List<UserTeam> selectedAccount = userTeamService.getBaseMapper().selectList(queryWrapper);

        return selectedAccount.size();
    }


}




