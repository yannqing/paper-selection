package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.common.DateFormat;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.service.UserTeamService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.CountOfTeamVo;
import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.JoinedTeacherStatusVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.wxxy.common.UserLoginState.SALT;
import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;
import static com.wxxy.utils.CheckLoginUtils.checkTeacherLoginStatus;
import static com.wxxy.utils.CheckLoginUtils.checkUserLoginStatus;

/**
* @author 67121
* @description 针对表【teacher(老师(队伍))】的数据库操作Service实现
* @createDate 2024-01-24 00:24:30
*/
@Slf4j
@ConfigurationProperties("project")
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>

    implements TeacherService{

    @Resource
    private UserService userService;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedisCache redisCache;

    @Value("${project.upload-url}")
    private String uploadUrl;

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
        User loginUser = CheckLoginUtils.checkUserLoginStatus(request, redisCache);
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
            studentGetTeachersVo.setApplyNum(teacher.getApplyNum());
            studentGetTeachersVo.setMaxApply(teacher.getMaxApply());
            studentGetTeachersVo.setCurrentNum(teacher.getCurrentNum());
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
     * @param teacherId 加入的老师id
     * @param userId 登录的用户id，前面无session时写的，后面可以优化掉
     * @return
     */
    @Override
    public boolean joinTeacher(Integer teacherId, Long userId, HttpServletRequest request) {
        checkUserLoginStatus(request, redisCache);

        if (teacherId == null) {
            throw new IllegalArgumentException("老师id不能为空");
        }
//        synchronized(teacherId) {
            //查询{已选择的老师数量}是否为2（达到最大）
            int teacherAccount = this.selectedTeacherAccount(userId, request);
            if (2 - teacherAccount <= 0) {
                throw new IllegalArgumentException("选择的老师数量超过可选择的范围！");
            }
            //查询对应老师的队伍人数限制和申请限制是否达到最大
            //1. 先查询老师的总人数限制 和申请人数限制
            Teacher teacher = teacherMapper.selectById(teacherId);
            Integer maxNum = teacher.getMaxNum();
            Integer maxApply = teacher.getMaxApply();
            //2. 再查询老师队伍的实际人数，和实际申请人数
            Integer currentNum = teacher.getCurrentNum();
            Integer applyNum = teacher.getApplyNum();
            if (currentNum >= maxNum) {
                throw new IllegalArgumentException("老师队伍已满，无法申请");
            } else if (applyNum >= maxApply) {
                throw new IllegalArgumentException("该老师队伍的申请已达最大限制，无法申请");
            } else if (userTeamService.getOne(new QueryWrapper<UserTeam>().eq("teacherId", teacherId).eq("userId", userId)) != null) {
                throw new IllegalArgumentException("已经申请加入此老师的队伍，请勿重复加入:" + teacherId);
            } else {
                //加入老师队伍
                UserTeam userTeam = new UserTeam();
                userTeam.setUserId(userId);
                userTeam.setTeacherId((long) teacherId);
                userTeamService.getBaseMapper().insert(userTeam);
                teacherMapper.update(new UpdateWrapper<Teacher>()
                        .eq("id", teacher.getId())
                        .set("applyNum", applyNum + 1));
            }
//        }
        return true;
    }

    /**
     * 查询该用户选择的老师队伍数量
     * @param userId
     * @return
     */
    @Override
    public int selectedTeacherAccount(Long userId, HttpServletRequest request) {
        checkUserLoginStatus(request, redisCache);

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
    public List<JoinedTeacherStatusVo> getJoinedTeacherStatus(Long userId, HttpServletRequest request) {
        checkUserLoginStatus(request,redisCache);

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

    /**
     * 上传头像（下载头像的接口在fileController中）
     * @param avatar
     * @param request
     * @return
     * @throws IOException
     */
    @Override
    public String uploadAvatar(MultipartFile avatar, HttpServletRequest request) throws IOException {
        //参数校验
        if (avatar == null) {
            throw new IllegalArgumentException("传入的头像为空，请重新上传！");
        }
        //权限校验
        Teacher teacher = checkTeacherLoginStatus(request, redisCache);
        //获取文件路径
        String fileName = avatar.getOriginalFilename();
        UUID uuid = UUID.randomUUID();
        String newFilename = replaceFilename(fileName, uuid.toString());
        Path path = Paths.get(uploadUrl + newFilename);
        //存储到服务器
        byte[] avatarBytes = avatar.getBytes();
        Files.write(path, avatarBytes);
        //下载路径
        return "http://localhost:8080/download/"+newFilename;
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
        User loginUser = CheckLoginUtils.checkUserLoginStatus(request, redisCache);

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
        User user = CheckLoginUtils.checkUserLoginStatus(request, redisCache);
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

        Integer applyNum = teacher.getApplyNum() - 1;
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("applyNum", applyNum));
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
        Teacher teacher = checkTeacherLoginStatus(request, redisCache);
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
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
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

    /**
     * 修改密码（老师）
     * @param oldPassword
     * @param newPassword
     * @param againPassword
     * @param request
     * @return
     */
    @Override
    public boolean changeMyPassword(String oldPassword, String newPassword, String againPassword, HttpServletRequest request) {
        //验证登录态
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
        Teacher teacher = teacherMapper.selectById(loginTeacher.getId());
        //确保两次输入密码相同
        if (!Objects.equals(newPassword, againPassword)) {
            throw new IllegalArgumentException("两次输入密码不同，请重试！");
        }
        String oldEncryptPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        String newEncryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        //判断原密码是否相同
        if (!oldEncryptPassword.equals(teacher.getUserPassword())) {
            throw new IllegalArgumentException("原密码错误，请重试！");
        }
        //修改密码
        int result = teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacher.getId())
                .set("userPassword", newEncryptPassword)
                .set("updateTime", DateFormat.getCurrentTime()));

        //移出登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        log.info("老师修改密码成功，请重新登录！");

        return result == 1;
    }

    /**
     * 更新个人信息（老师）
     * @param updateTeacher
     * @param request
     * @return
     */
    @Override
    public boolean updateMyselfInfo(Teacher updateTeacher, HttpServletRequest request) {
        //校验登录态
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
        //更新个人信息
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginTeacher.getId());
        updateWrapper.set("name", updateTeacher.getName());
        updateWrapper.set("avatarUrl", updateTeacher.getAvatarUrl());
        updateWrapper.set("description", updateTeacher.getDescription());
        updateWrapper.set("phone", updateTeacher.getPhone());
        updateWrapper.set("email", updateTeacher.getEmail());
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = teacherMapper.update(null, updateWrapper);

        return result == 1;
    }

    @Override
    public boolean changeApplySize(Integer applySize, HttpServletRequest request) {
        //查询参数是否合法
        if (applySize <= 0) {
            throw new IllegalArgumentException("参数不合法，申请容量不能<=0");
        }
        //校验登录态
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
        //查询修改的容量是否小于已经申请的容量
        if (loginTeacher.getApplyNum() >= applySize) {
            throw new IllegalArgumentException("申请限制不能小于已申请数量！");
        }
        //修改申请容量
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", loginTeacher.getId())
                .set("maxApply", applySize)
                .set("updateTime", DateFormat.getCurrentTime()));

        log.info("老师{name：{}} 修改申请限制{size: {}}成功！", loginTeacher.getName(), applySize);
        return true;
    }

    @Override
    public boolean changeTeamMaxSize(Integer teamMaxNum, HttpServletRequest request) {
        //参数校验
        if (teamMaxNum < 0) {
            throw new IllegalArgumentException("队伍最大数量不能小于0");
        }
        //校验登录态
        Teacher loginTeacher = checkTeacherLoginStatus(request, redisCache);
        //查寻要修改的数量是否小于队伍中已存在的用户数量
        if (loginTeacher.getCurrentNum() > teamMaxNum) {
            throw new IllegalArgumentException("要修改的数量不能低于队伍中已有的成员数量，若要修改，请先移出部分成员");
        }
        //修改最大数量
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", loginTeacher.getId());
        updateWrapper.set("maxNum", teamMaxNum);
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = teacherMapper.update(updateWrapper);
        log.info("老师 name：{} 修改队伍人数限制成功！", loginTeacher.getName());
        return result == 1;
    }

    /**
     * 自定义方法，用来替换文件名
     * @param filename
     * @param newName
     * @return
     */
    public static String replaceFilename(String filename, String newName) {
        // 使用正则表达式匹配文件名和扩展名部分
        String regex = "(.*)(\\..*)";
        // 将文件名替换为新名称
        return filename.replaceAll(regex, newName + "$2");
    }
}




