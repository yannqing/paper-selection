package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.wxxy.service.impl.AuthServiceImpl.SALT;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Override
    public boolean addUser(User user) {
        //参数校验
        if (user.getUsername() == null) {
            throw new IllegalArgumentException("学生名字不能为空");
        }
        if (user.getUserAccount() == null) {
            throw new IllegalArgumentException("学生学号不能为空");
        }
        if (user.getAcademy() == null) {
            throw new IllegalArgumentException("学生学院不能为空");
        }
        if (user.getDegree() == null) {
            throw new IllegalArgumentException("学生专业不能为空");
        }
        if (user.getPhone() == null) {
            throw new IllegalArgumentException("学生电话不能为空");
        }
        //学号不能重复
        if (userMapper.selectOne(new QueryWrapper<User>().eq("userAccount", user.getUserAccount())) != null) {
            throw new IllegalArgumentException("学号已存在，无法新增");
        }
        //设置默认密码为123456
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + "123456").getBytes());
        user.setUserPassword(encryptPassword);
        int result = userMapper.insert(user);
        return result == 1;
    }

    @Override
    public boolean addTeacher(Teacher teacher) {
        //参数校验
        if (teacher.getName() == null) {
            throw new IllegalArgumentException("教师名称不能为空");
        }
        if (teacher.getUserAccount() == null) {
            throw new IllegalArgumentException("教师账户不能为空");
        }
        if (teacher.getPhone() == null) {
            throw new IllegalArgumentException("教师电话不能为空");
        }
        //学号不能重复
        if (teacherMapper.selectOne(new QueryWrapper<Teacher>().eq("userAccount", teacher.getUserAccount())) != null) {
            throw new IllegalArgumentException("此账号已存在，无法新增");
        }
        //设置默认值
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + "123456").getBytes());
        teacher.setUserPassword(encryptPassword);
        int result = teacherMapper.insert(teacher);
        return result == 1;
    }

    @Override
    public boolean deleteUser(Long userId) {
        //参数校验
        if (userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("学生不存在，无法删除");
        }
        //删除
        int result = userMapper.delete(new QueryWrapper<User>().eq("id", userId));
        return result == 1;
    }

    @Override
    public boolean deleteTeacher(Long teacherId) {
        //参数校验
        if (teacherId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (teacherMapper.selectById(teacherId) == null) {
            throw new IllegalArgumentException("学生不存在，无法删除");
        }
        //删除
        int result = teacherMapper.delete(new QueryWrapper<Teacher>().eq("id", teacherId));
        return result == 1;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return teacherMapper.selectList(null);
    }

    @Override
    public List<User> getUsersUnselecting() {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("isJoin", 1);
        List<UserTeam> userTeams = userTeamMapper.selectList(userTeamQueryWrapper);
        Set<Long> joinedUserIds = new HashSet<>();
        for (UserTeam team : userTeams) {
            joinedUserIds.add(team.getUserId());
        }
        List<User> users = userMapper.selectList(null);
        for (int i = 0; i < users.size(); ) {
            if (joinedUserIds.contains(users.get(i).getId())) {
                users.remove(i);
            }else {
                i++;
            }
        }

        return users;
    }
}
