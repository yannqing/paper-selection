package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.SecondService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Resource
    private RedisCache redisCache;

    @Resource
    private ObjectMapper objectMapper;


    @Override
    public Object login(String username, String password, HttpServletRequest request) {
        //1. 先检测是否是学生登录
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
        queryUserWrapper.eq("userAccount",username);
        queryUserWrapper.eq("userPassword",encryptPassword);
        queryUserWrapper.eq("userStatus", 0);
        User user = userMapper.selectOne(queryUserWrapper);
        if (user != null && user.getUserRole() == 1) {
            user.setUserPassword(null);
            //记录用户登录态
            request.getSession().setAttribute(USER_LOGIN_STATE, user);
            log.info("管理员: "+ user.getUsername() +" 登录成功！");
            return user;
        }
        if (user != null && user.getUserStatus() != 1) {
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

    @Override
    public GetAllByPageVo<Teacher> getFirstTeacherMessage(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) throws JsonProcessingException {
        checkRole(request);

        //1. 从redis中取数据
        String firstResult = redisCache.getCacheObject("firstResult:teacherMessage");
        List<Teacher> firstResultTeachers = objectMapper.readValue(firstResult, new TypeReference<List<Teacher>>() {});
        int total = firstResultTeachers.size();
        //2. 根据输入的关键字进行第一次查询
        if (searchAccount != null || !searchAccount.equals("")) {
            firstResultTeachers = firstResultTeachers.stream()
                    .filter(teacher -> teacher.getUserAccount().contains(searchAccount)) // 根据关键字过滤
                    .collect(Collectors.toList());
        }
        //3. 定义结果集合，存放查询的结果
        List<Teacher> resultTeachers = new ArrayList<Teacher>();
        //4. 进行分页
        if (currentPage == null || pageSize == null) {
            //如果传入的分页参数是空，则查询第一页，10条数据
            for (int i = 0; i < 10; i++) {
                resultTeachers.add(firstResultTeachers.get(i));
            }
        } else {
            //否则则按要求来查询
            // 计算分页的起始索引和结束索引
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, firstResultTeachers.size());
            for (int i = startIndex; i < endIndex; i++) {
                resultTeachers.add(firstResultTeachers.get(i));
            }
        }

        return new GetAllByPageVo<Teacher>(resultTeachers, total);
    }

    @Override
    public List<User> getFirstJoinedTeamUsers(HttpServletRequest request, Long teacherId) throws JsonProcessingException {

        checkRole(request);

        //1. 从redis中取数据
        String firstResult = redisCache.getCacheObject("firstResult:joinedMessage");
        List<UserTeam> firstResultUserTeam = objectMapper.readValue(firstResult, new TypeReference<List<UserTeam>>() {});
        //2. 查询属于老师队伍的所有成员id
        List<Long> userIds = new ArrayList<Long>();
        for (UserTeam team : firstResultUserTeam) {
            if (Objects.equals(team.getTeacherId(), teacherId) && team.getIsJoin() == 1) {
                userIds.add(team.getUserId());
            }
        }
        //3. 根据id查询所有的用户，并返回
        return userMapper.selectBatchIds(userIds);

    }


    public void checkRole(HttpServletRequest request) {
        User user = CheckLoginUtils.checkUserLoginStatus(request);
        if (user.getUserRole() == 0) {
            throw new IllegalArgumentException("您没有权限，请重试");
        }
    }



}
