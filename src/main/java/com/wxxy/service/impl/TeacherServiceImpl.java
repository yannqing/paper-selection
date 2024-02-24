package com.wxxy.service.impl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.TeacherService;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.UserService;
import com.wxxy.service.UserTeamService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.vo.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

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

    /**
     * 查询全部老师信息
     * @param currentPage 当前页码
     * @param pageSize 一页的数据条数
     * @param request 获取session
     * @return
     */
    @Override
    public GetAllByPageVo<StudentGetTeachersVo> getAllTeachers(Integer currentPage, Integer pageSize, HttpServletRequest request) {
        //查看登录状态
        User loginUser = CheckLoginUtils.checkUserLoginStatus(request);
        //分页查询数据
        Page<Teacher> pageConfig ;
            //如果传入的分页参数是空，则查询第一页，10条数据
        if (currentPage == null || pageSize == null) {
            pageConfig = new Page<>();
        } else {
            pageConfig = new Page<>(currentPage, pageSize);
        }
        Page<Teacher> teacherPage = teacherMapper.selectPage(pageConfig, null);
        //获取数据和总数
        List<Teacher> teachers = teacherPage.getRecords();
        long total = teacherPage.getTotal();
        //对获取的数据进行二次处理，得到前端需要的数据
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
            //设置各个老师对自己的状态
            QueryWrapper<UserTeam> userTeamQueryWrapper1 = new QueryWrapper<>();
            userTeamQueryWrapper1.eq("teacherId", teacher.getId());
            userTeamQueryWrapper1.eq("userId", loginUser.getId());
            UserTeam myRelativeTeam = userTeamService.getBaseMapper().selectOne(userTeamQueryWrapper1);
            if (myRelativeTeam == null) {
                studentGetTeachersVo.setStatus(0);
            }
            else if (myRelativeTeam.getIsJoin() == 0) {
                studentGetTeachersVo.setStatus(1);
            } else {
                studentGetTeachersVo.setStatus(2);
            }

            studentGetTeachersVos.add(studentGetTeachersVo);
        }
        //返回结果
        return new GetAllByPageVo<>(studentGetTeachersVos, total);
    }

    /**
     * 用户加入老师队伍
     * @param teacherIds 加入的老师id数组，最大2个
     * @param userId 登录的用户id，前面无session时写的，后面可以优化掉
     * @return
     */
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
        //查询对应老师的队伍人数限制和申请限制是否达到最大
        for (int teacherId : teacherIds) {
            //1. 先查询老师的总人数限制 和申请人数限制
            Teacher teacher = teacherMapper.selectById(teacherId);
            Integer maxNum = teacher.getMaxNum();
            Integer maxApply = teacher.getMaxApply();
            //2. 再查询老师队伍的实际人数，和实际申请人数
            Integer currentNum = teacher.getCurrentNum();
            Integer applyNum = teacher.getApplyNum();
            if (currentNum >= maxNum) {
                throw new IllegalArgumentException("老师队伍已满，无法申请");
            }
            else if (applyNum >= maxApply) {
                throw new IllegalArgumentException("该老师队伍的申请已达最大限制，无法申请");
            }
            else if(userTeamService.getOne(new QueryWrapper<UserTeam>().eq("teacherId",teacherId).eq("userId",userId))!=null){
                throw new IllegalArgumentException("已经申请加入此老师的队伍，请勿重复加入:"+teacherId);
            }
            else {
                //加入老师队伍
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeacherId((long) teacherId);
                userTeamService.getBaseMapper().insert(userTeam);
                teacherMapper.update(new UpdateWrapper<Teacher>()
                        .eq("id", teacher.getId())
                        .set("applyNum", applyNum + 1));
            }
        }
        return true;
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
            throw new IllegalArgumentException("此用户id不存在");
        }
        //查询此用户申请的队伍数量
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("isJoin", 0);
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
            throw new IllegalArgumentException("此用户id不存在");
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
        Teacher teacher = CheckLoginUtils.checkTeacherLoginStatus(request);
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
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("老师不存在");
        }
        Integer currentNum = teacher.getCurrentNum();

        //获取到此用户的信息
        User loginUser = CheckLoginUtils.checkUserLoginStatus(request);

        //退出队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.eq("userId", loginUser.getId());
        queryWrapper.eq("isJoin", 1);
        //判断用户是否加入此队伍
        if (userTeamService.getOne(queryWrapper) == null) {
            throw new IllegalArgumentException("用户: "+loginUser.getUsername()+" 并未成功加入此队伍teacherId: "+teacherId+"，退出失败");
        }
        int deleteResult = userTeamService.getBaseMapper().delete(queryWrapper);

        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("currentNum", currentNum - 1));

        return deleteResult == 1;
    }

    /**
     * 取消申请加入
     * @param teacherId
     * @param request
     * @return
     */
    @Override
    public boolean cancelApplication(Long teacherId, HttpServletRequest request) {
        //查询是否登录
        User user = CheckLoginUtils.checkUserLoginStatus(request);
        //查询传入的参数是否为空
        if (teacherId == null) {
            throw new IllegalArgumentException("队伍id为空，无法确定取消哪个队伍的申请");
        }
        //查询传入的teacherId是否存在
        QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
        teacherQueryWrapper.eq("id", teacherId);
        Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
        if (teacher == null) {
            throw new IllegalArgumentException("此老师队伍不存在，请确认后再取消");
        }
        //查询学生对老师的申请
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", user.getId());
        userTeamQueryWrapper.eq("teacherId", teacherId);
        UserTeam apply = userTeamService.getBaseMapper().selectOne(userTeamQueryWrapper);
        if (apply == null) {
            throw new IllegalArgumentException("您并未向此老师提出申请加入队伍，无法取消");
        }
        //如果老师已经同意申请，那么无法进行取消
        if (apply.getIsJoin() == 1) {
            throw new IllegalArgumentException("老师已同意申请，无法取消");
        }
        int result = userTeamService.getBaseMapper().delete(userTeamQueryWrapper);
        return result == 1;
    }

    /**
     * 查看队伍容量
     * @param request 获取session
     * @return
     */
    @Override
    public CountOfTeamVo getCountOfTeam(HttpServletRequest request) {
        //1. 查询登录状态
        Teacher teacher = CheckLoginUtils.checkTeacherLoginStatus(request);
        //2. 新建返回变量
        CountOfTeamVo result = new CountOfTeamVo();
        //3. 获取队伍最大人数，并赋值
        result.setMaxNum(teacherMapper.selectOne(new QueryWrapper<Teacher>().eq("id", teacher.getId())).getMaxNum());
        //4. 获取已申请的人数
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teacherId", teacher.getId());
        userTeamQueryWrapper.eq("isJoin", 0);
        Long selectedCount = userTeamService.getBaseMapper().selectCount(userTeamQueryWrapper);
        result.setSelectedNum(selectedCount);
        //5. 获取已加入的人数
        QueryWrapper<UserTeam> userTeamQueryWrapper1 = new QueryWrapper<>();
        userTeamQueryWrapper1.eq("teacherId", teacher.getId());
        userTeamQueryWrapper1.eq("isJoin", 1);
        Long joinedCount = userTeamService.getBaseMapper().selectCount(userTeamQueryWrapper1);
        result.setJoinedNum(joinedCount);

        return result;
    }

    /**
     * 获取个人信息（老师）
     * @param request 获取session
     * @return
     */
    @Override
    public Teacher getMyselfInfo(HttpServletRequest request) {
        //查看是否登录
        Teacher loginTeacher = CheckLoginUtils.checkTeacherLoginStatus(request);
        //获取个人信息
        Teacher teacherMsg = teacherMapper.selectOne(new QueryWrapper<Teacher>().eq("id", loginTeacher.getId()));
        //脱敏
//        TeacherVo teacherVo = new TeacherVo();
//        teacherVo.setId(teacherMsg.getId());
//        teacherVo.setName(teacherMsg.getName());
//        teacherVo.setUserAccount(teacherMsg.getUserAccount());
//        teacherVo.setAvatarUrl(teacherMsg.getAvatarUrl());
//        teacherVo.setDescription(teacherMsg.getDescription());
//        teacherVo.setPhone(teacherMsg.getPhone());
//        teacherVo.setEmail(teacherMsg.getEmail());
//        teacherVo.setMaxNum(teacherMsg.getMaxNum());
        teacherMsg.setUserPassword(null);
        return teacherMsg;
    }


}




