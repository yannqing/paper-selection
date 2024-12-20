package com.wxxy.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.common.DateFormat;
import com.wxxy.domain.ExportExcelData;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.AdminService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.utils.RedisCache;
import com.wxxy.vo.GetAllByPageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import static com.wxxy.common.UserLoginState.SALT;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    RedisCache redisCache;

    @Value("${project.export-url}")
    private String exportUrl;


    @Override
    public boolean addUser(User user, HttpServletRequest request) {
        checkRole(request);
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
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());
        user.setUserPassword(encryptPassword);
        int result = userMapper.insert(user);
        return result == 1;
    }

    @Override
    public boolean addTeacher(Teacher teacher, HttpServletRequest request) {
        checkRole(request);
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
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + teacher.getUserPassword()).getBytes());
        teacher.setUserPassword(encryptPassword);
        int result = teacherMapper.insert(teacher);
        return result == 1;
    }

    @Override
    public boolean deleteUser(Long userId, HttpServletRequest request) {
        checkRole(request);
        //参数校验
        if (userId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("学生不存在，无法删除");
        }
        //删除学生
        int result = userMapper.delete(new QueryWrapper<User>().eq("id", userId));
        // 老师的队伍/申请 - 1
        List<UserTeam> userTeams = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("userId", userId));
        for(UserTeam userTeam : userTeams) {
            Long teacherId = userTeam.getTeacherId();
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (userTeam.getIsJoin() == 0) {
                teacherMapper.update(new UpdateWrapper<Teacher>().eq("id", teacherId).set("applyNum", teacher.getApplyNum() - 1));
            } else {
                teacherMapper.update(new UpdateWrapper<Teacher>().eq("id", teacherId).set("applyNum", teacher.getCurrentNum() - 1));
            }
        }
        //删除学生的所有入队信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamMapper.delete(userTeamQueryWrapper);
        return result == 1;
    }

    @Override
    public boolean deleteTeacher(Long teacherId, HttpServletRequest request) {
        checkRole(request);
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
    public GetAllByPageVo<User> getAllUsers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        checkRole(request);
        //分页查询数据
        Page<User> pageConfig ;
        //如果传入的分页参数是空，则查询第一页，10条数据
        if (currentPage == null || pageSize == null) {
            pageConfig = new Page<>();
        } else {
            pageConfig = new Page<>(currentPage, pageSize);
        }
        Page<User> userPage;

        if (searchAccount == null) {
            userPage = userMapper.selectPage(pageConfig, new QueryWrapper<User>().eq("userRole", 0));
        } else {
            QueryWrapper<User> query = new QueryWrapper<>();
            query.like("userAccount", searchAccount);
            query.eq("userRole", 0);
            userPage = userMapper.selectPage(pageConfig, query);
        }

        List<User> userList = userPage.getRecords();
        long total = userPage.getTotal();

        return new GetAllByPageVo<>(userList, total);
    }

    @Override
    public GetAllByPageVo<Teacher> getAllTeachers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        checkRole(request);
        //分页查询数据
        Page<Teacher> pageConfig ;
        //如果传入的分页参数是空，则查询第一页，10条数据
        if (currentPage == null || pageSize == null) {
            pageConfig = new Page<>();
        } else {
            pageConfig = new Page<>(currentPage, pageSize);
        }
        Page<Teacher> teacherPage;

        if (searchAccount == null) {
            teacherPage = teacherMapper.selectPage(pageConfig, null);
        } else {
            QueryWrapper<Teacher> query = new QueryWrapper<>();
            query.like("userAccount", searchAccount);
            teacherPage = teacherMapper.selectPage(pageConfig, query);
        }
        List<Teacher> userList = teacherPage.getRecords();
        long total = teacherPage.getTotal();
        return new GetAllByPageVo<>(userList, total);
    }

    @Override
    public GetAllByPageVo<User> getUsersUnselecting(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {

        //1. 检查是否登录
        checkRole(request);
        //2. 查询user-team表，找出所有加入队伍学生的id
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("isJoin", 1);
        List<UserTeam> userTeams = userTeamMapper.selectList(userTeamQueryWrapper);
        //3. 将查询的结果存入set集合joinedUserIds
        Set<Long> joinedUserIds = new HashSet<>();
        for (UserTeam team : userTeams) {
            joinedUserIds.add(team.getUserId());
        }
        //4. 分页，搜索查询
        //5. 判断是否传入分页的参数，不传入则默认查询第一页，10条
        Page<User> userSearchPage;
        if (currentPage == null || pageSize == null) {
            userSearchPage = new Page<>();
        }else {
            userSearchPage = new Page<>(currentPage, pageSize);
        }
        //6. 查询的结果存入userResultPage
        Page<User> userResultPage;
        //7. 查询的条件：不能等于已经加入的学生id
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (Long id : joinedUserIds) {
            userQueryWrapper.ne("id", id);
            userQueryWrapper.eq("userRole", 0);
        }
        //8. 如果有查询条件，则加上模糊查询
        if (searchAccount != null) {
            userQueryWrapper.like("userAccount", searchAccount);
        }
        //9. 查询结果
        userResultPage = userMapper.selectPage(userSearchPage, userQueryWrapper);
        //10. 获取数据
        List<User> users = userResultPage.getRecords();
        long total = userResultPage.getTotal();
        //11. 封装，返回
        return new GetAllByPageVo<>(users, total);
    }

    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        checkRole(request);
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId());
        updateWrapper.set("username", user.getUsername());
        updateWrapper.set("academy", user.getAcademy());
        updateWrapper.set("degree", user.getDegree());
        updateWrapper.set("userAccount", user.getUserAccount());
//        updateWrapper.set("userPassword", DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes()));
        updateWrapper.set("gender", user.getGender());
        updateWrapper.set("profile", user.getProfile());
        updateWrapper.set("phone", user.getPhone());
        updateWrapper.set("email", user.getEmail());
        updateWrapper.set("userStatus", user.getUserStatus());
        int result = userMapper.update(null, updateWrapper);
        return result == 1;
    }

    @Override
    public boolean updateTeacher(Teacher teacher, HttpServletRequest request) {
        checkRole(request);
        if (teacher == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teacher.getId());
        updateWrapper.set("name", teacher.getName());
        updateWrapper.set("userAccount", teacher.getUserAccount());
//        updateWrapper.set("userPassword", DigestUtils.md5DigestAsHex((SALT + teacher.getUserPassword()).getBytes()));
        updateWrapper.set("avatarUrl", teacher.getAvatarUrl());
        updateWrapper.set("description", teacher.getDescription());
        updateWrapper.set("phone", teacher.getPhone());
        updateWrapper.set("email", teacher.getEmail());
        updateWrapper.set("maxNum", teacher.getMaxNum());
        updateWrapper.set("maxApply", teacher.getMaxApply());
        updateWrapper.set("status", teacher.getStatus());
        int result = teacherMapper.update(null, updateWrapper);
        return result == 1;
    }

    /**
     * @param studentExcel
     * @param request
     * @return 1有重复，0无重复
     * @throws IOException
     */
    @Override
    public String uploadExcelStudent(MultipartFile studentExcel, HttpServletRequest request) throws IOException {
        checkRole(request);
        if (studentExcel.isEmpty()) {
            throw new IllegalArgumentException("传入的文件为空");
        }
            // 创建工作簿对象，加载上传的文件
            Workbook workbook = new XSSFWorkbook(studentExcel.getInputStream());
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            // 遍历工作表中的行
            Iterator<Row> rowIterator = sheet.iterator();
//            List<List<String>> data = new ArrayList<>();
            List<User> users = new ArrayList<>();
            //遍历每行
            int rowIndex = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
//                List<String> rowData = new ArrayList<>();
                if (rowIndex++ == 0) {
                    continue;
                }
                int cellIndex = 0 ;
                User user = new User();
                while (cellIndex <=8) {
                    String cellValue = null;
                    Cell cell = row.getCell(cellIndex);
                    if (cell != null) {
                        if (cell.getCellType() == CellType.NUMERIC) {
                            // 将NUMERIC类型的单元格转换为STRING类型
                            cell.setCellType(CellType.STRING);
                        }
                        cellValue = cell.getStringCellValue();
                    }

                    switch (cellIndex) {
                        case 0: user.setUsername(cellValue); break;
                        case 1: user.setAcademy(cellValue); break;
                        case 2: user.setDegree(cellValue); break;
                        case 3: user.setUserAccount(cellValue); break;
//                        case 4: {
//                            if (cellValue != null) {
//                                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes()));
//                            }else {
//                                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
//                            }
//                            break;
//                        }
                        case 4: {
                            if (cellValue != null) {
                                user.setGender(cellValue.equals("男")?1:0);
                            }else{
                                user.setGender(1);
                            }
                            break;
                        }
                        case 5: user.setProfile(cellValue); break;
                        case 6: user.setPhone(cellValue); break;
                        case 7: user.setEmail(cellValue);
                    }
//                    rowData.add(cellValue);
                    cellIndex++;
                }
                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
                users.add(user);
//                data.add(rowData);
                rowIndex ++;
            }
            workbook.close();
//            System.out.println(data);
            //查重
            for (int i = 0 ; i < users.size() ; ) {
                if (!userMapper.exists(new QueryWrapper<User>().eq("userAccount", users.get(i).getUserAccount()))){
                    userMapper.insert(users.get(i));
                    users.remove(i);
                } else {
                    i ++;
                }
            }
            if (users.size() != 0) {
                String uuid = UUID.randomUUID().toString();
                redisCache.setCacheObject("student:excel:"+uuid, objectMapper.writeValueAsString(users));
                return uuid;
            }
            return null;
    }

    @Override
    public String uploadExcelTeacher(MultipartFile teacherExcel, HttpServletRequest request) throws IOException {
        checkRole(request);
        if (teacherExcel.isEmpty()) {
            throw new IllegalArgumentException("传入的文件为空");
        }
        // 创建工作簿对象，加载上传的文件
        Workbook workbook = new XSSFWorkbook(teacherExcel.getInputStream());
        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);
        // 遍历工作表中的行
        Iterator<Row> rowIterator = sheet.iterator();
//            List<List<String>> data = new ArrayList<>();
        List<Teacher> teachers = new ArrayList<>();
        //遍历每行
        int rowIndex = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
//                List<String> rowData = new ArrayList<>();
            if (rowIndex++ == 0) {
                continue;
            }
            int cellIndex = 0 ;
            Teacher teacher = new Teacher();
            while (cellIndex <= 5) {
                String cellValue = null;
                Cell cell = row.getCell(cellIndex);
                if (cell != null) {
                    if (cell.getCellType() == CellType.NUMERIC) {
                        // 将NUMERIC类型的单元格转换为STRING类型
                        cell.setCellType(CellType.STRING);
                    }
                    cellValue = cell.getStringCellValue();
                }
                switch (cellIndex) {
                    case 0: teacher.setName(cellValue); break;
                    case 1: teacher.setUserAccount(cellValue); break;
//                    case 2: {
//                        if (cellValue != null) {
//                            teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes()));
//                        }else {
//                            teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
//                        }
//                        break;
//                    }
                    case 2: teacher.setDescription(cellValue); break;
                    case 3: teacher.setPhone(cellValue); break;
                    case 4: teacher.setEmail(cellValue);
                }
//                    rowData.add(cellValue);
                cellIndex++;
            }
            teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
            teachers.add(teacher);
//                data.add(rowData);
            rowIndex ++;
        }
        workbook.close();
//            System.out.println(data);
        //查重
        for (int i = 0 ; i < teachers.size(); ) {
            if (!teacherMapper.exists(new QueryWrapper<Teacher>().eq("userAccount", teachers.get(i).getUserAccount()))){
                teacherMapper.insert(teachers.get(i));
                teachers.remove(i);
            }
            else {
                i ++;
            }
        }
        if (teachers.size() != 0) {
            String uuid = UUID.randomUUID().toString();
            redisCache.setCacheList("teacher:excel:"+uuid, teachers);
            return uuid;
        }
        return null;
    }

    /**
     * 查看老师的队伍
     * @param request
     * @return
     */
    @Override
    public List<User> joinedStudent(HttpServletRequest request, Integer teacherId) {
        //鉴权
        checkRole(request);
        //查询我的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacherId", teacherId);
        queryWrapper.eq("isJoin", 1);
        List<UserTeam> myTeams = userTeamMapper.selectList(queryWrapper);
        List<User> joinedUsers = new ArrayList<>();
        for (UserTeam userTeam : myTeams) {
            QueryWrapper<User> queryUserWrapper = new QueryWrapper<>();
            queryUserWrapper.eq("id", userTeam.getUserId());
            User joinedUser = userMapper.selectOne(queryUserWrapper);
            //脱敏
            joinedUser.setUserPassword(null);
            joinedUsers.add(joinedUser);
        }
        log.info("查看老师：" + teacherId + " 的队伍成功！");
        return joinedUsers;
    }

    /**
     * 重置学生密码
     * @param userId
     * @param request
     * @return
     */
    @Override
    public boolean resetStudentPassword(Long userId, HttpServletRequest request) {
        log.info("重置学生密码");
        //鉴权
        checkRole(request);
        //参数校验
        if (userId == null) {
            throw new IllegalArgumentException("传入的学生id不能为空！");
        } else {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new IllegalArgumentException("该学生不存在，请重试！");
            }
        }
        int result = userMapper.update(new UpdateWrapper<User>()
                .eq("id", userId)
                .set("userPassword", DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()))
                .set("updateTime", DateFormat.getCurrentTime()));
        return result == 1;
    }

    /**
     * 重置老师密码
     * @param teacherId
     * @param request
     * @return
     */
    @Override
    public boolean resetTeacherPassword(Long teacherId, HttpServletRequest request) {
        log.info("重置老师密码");
        //鉴权
        checkRole(request);
        //参数校验
        if (teacherId == null) {
            throw new IllegalArgumentException("传入的老师id不能为空！");
        } else {
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (teacher == null) {
                throw new IllegalArgumentException("该老师不存在，请重试！");
            }
        }
        int result = teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("userPassword", DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()))
                .set("updateTime", DateFormat.getCurrentTime()));
        return result == 1;
    }

    /**
     * 覆盖Excel数据
     *
     * @param isCover 1为覆盖，0为不覆盖
     * @param role    1学生，0老师
     * @param request
     */
    @Override
    public void isCover(String isCover, int role, HttpServletRequest request) throws JsonProcessingException {
        //鉴权
        checkRole(request);
        //检查是否覆盖
        if (isCover == null) {
            //不覆盖则删除redis数据
            if (role == 1) {
                redisCache.deleteObject("student:excel");
            }else {
                redisCache.deleteObject("teacher:excel");
            }
        }else {
            if (role == 1) {
                String studentInfo = redisCache.getCacheObject("student:excel:"+isCover);
                List<User> students = objectMapper.readValue(studentInfo, new TypeReference<List<User>>() {});
                if (students == null || students.size() == 0) {
                    throw new IllegalArgumentException("覆盖失败，请重试");
                }
                students.forEach(user -> {
                    userMapper.update(new UpdateWrapper<User>().
                            eq("userAccount", user.getUserAccount())
                            .set("username", user.getUsername())
                            .set("academy", user.getAcademy())
                            .set("degree", user.getDegree())
                            .set("profile", user.getProfile())
                            .set("phone", user.getPhone())
                            .set("email", user.getEmail())
                            .set("gender", user.getGender())
                            .set("userPassword", user.getUserPassword()));
                });
                redisCache.deleteObject("student:excel");
            }else {
                String teacherInfo = redisCache.getCacheObject("teacher:excel:"+isCover);
                List<Teacher> teachers = objectMapper.readValue(teacherInfo, new TypeReference<List<Teacher>>() {});
                if (teachers == null || teachers.size() == 0) {
                    throw new IllegalArgumentException("覆盖失败，请重试");
                }
                teachers.forEach(teacher -> {
                    teacherMapper.update(new UpdateWrapper<Teacher>()
                            .eq("userAccount", teacher.getUserAccount())
                            .set("name", teacher.getName())
                            .set("userPassword", teacher.getUserPassword())
                            .set("description", teacher.getDescription())
                            .set("phone", teacher.getPhone())
                            .set("email", teacher.getEmail()));
                });
                redisCache.deleteObject("teacher:excel");
            }
        }

    }

    /**
     * 修改所有老师队伍限制人数
     * @param teamSize
     * @param request
     * @return 如果是true，则全部修改成功，否则的话，队伍人数大于修改的限制，无法修改
     */
    @Override
    public boolean changeAllTeachersTeamSize(Integer teamSize, HttpServletRequest request) {
        //鉴权
        checkRole(request);
        //
        boolean result = true;
        log.info("修改全部老师的队伍人数限制");
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher : teachers) {
            if (teacher.getCurrentNum() <= teamSize) {
                teacherMapper.update(new UpdateWrapper<Teacher>()
                        .eq("id", teacher.getId())
                        .set("maxNum", teamSize));
            }
            else {
                result = false;
                log.info("修改全部老师的队伍人数限制：存在修改失败的老师！");
            }
        }
        return result;
    }

    /**
     * 修改全部老师的队伍人申请制
     * @param applySize
     * @param request
     * @return 如果是true，则全部修改成功，否则的话，存在修改失败的老师
     */
    @Override
    public boolean changeAllTeachersApplySize(Integer applySize, HttpServletRequest request) {
        checkRole(request);
        boolean result = true;
        log.info("修改全部老师的队伍人申请制");
        List<Teacher> teachers = teacherMapper.selectList(null);
        for (Teacher teacher : teachers) {
            if (teacher.getApplyNum() <= applySize) {
                teacherMapper.update(new UpdateWrapper<Teacher>()
                        .eq("id", teacher.getId())
                        .set("maxApply", applySize));
            }
            else {
                result = false;
                log.info("修改全部老师的队伍申请限制：存在修改失败的老师！");
            }
        }
        return result;
    }
    /**
     * 更改队伍容量
     * @param maxSize 要修改的容量
     * @param teacherId 要修改的老师id
     * @param request 获取老师信息
     * @return
     */
    @Override
    public boolean changeMaxSize(int maxSize, int teacherId, HttpServletRequest request) {
        //参数校验
        if (maxSize < 0) {
            throw new IllegalArgumentException("队伍最大数量不能小于0");
        }
        //鉴权
        checkRole(request);
        //查寻要修改的数量是否小于队伍中已存在的用户数量
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher.getCurrentNum() > maxSize) {
            throw new IllegalArgumentException("要修改的数量不能低于队伍中已有的成员数量，若要修改，请先移出部分成员");
        }
        //修改最大数量
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teacherId);
        updateWrapper.set("maxNum", maxSize);
        updateWrapper.set("updateTime", DateFormat.getCurrentTime());
        int result = teacherMapper.update(updateWrapper);
        log.info("修改老师{id："+teacherId+"} 的队伍人数限制成功！");
        return result == 1;
    }

    /**
     * 更改申请容量
     * @param applySize 最新申请限制数量
     * @param teacherId 要修改的老师id
     * @param request 验证登录
     * @return
     */
    @Override
    public boolean changeApplySize(int applySize, int teacherId, HttpServletRequest request) {
        //查询参数是否合法
        if (applySize <= 0) {
            throw new IllegalArgumentException("参数不合法，申请容量不能<=0");
        }
        //查询是否登录
        checkRole(request);
        //查询修改的容量是否小于已经申请的容量
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher.getApplyNum() >= applySize) {
            throw new IllegalArgumentException("申请限制不能小于已申请数量！");
        }
        //修改申请容量
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("maxApply", applySize)
                .set("updateTime", DateFormat.getCurrentTime()));

        log.info("修改老师{id："+teacherId+"} 的申请限制成功！");
        return true;
    }

    /**
     * 从队伍中删除某个学生
     * @param userId
     * @param teacherId
     * @param request
     * @return
     */
    @Override
    public boolean removeFromTeam(Long userId, Long teacherId, HttpServletRequest request) {
        //鉴权
        checkRole(request);
        //查询用户是否加入老师队伍，并移除
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teacherId", teacherId);
        int delete = userTeamMapper.delete(userTeamQueryWrapper);
        if (delete == 0) {
            throw new IllegalArgumentException("该用户未加入此队伍，无法删除，请重试");
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        int joinedNum = teacher.getCurrentNum();
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("currentNum", joinedNum - 1));

        log.info("管理员删除队伍（id："+teacherId + "）中的成员（id：" + userId + "）成功！");
        return true;
    }

    /**
     * 随机分配
     * @param request
     * @return
     * @throws InterruptedException
     */
    @Override
    public Integer distribute(HttpServletRequest request) throws InterruptedException {
        log.info("管理员第三轮筛选：随机分配！");
        //鉴权
        checkRole(request);
        //随机分配
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("isJoin", 1);
        List<UserTeam> userTeams = userTeamMapper.selectList(userTeamQueryWrapper);
        Set<Long> joinedUserIds = new HashSet<>();
        for (UserTeam team : userTeams) {
            joinedUserIds.add(team.getUserId());
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        for (Long id : joinedUserIds) {
            userQueryWrapper.ne("id", id);
        }
        //所有未加入队伍的学生
        List<User> users = userMapper.selectList(userQueryWrapper);
        int userSize = users.size();
        List<Teacher> teachers = teacherMapper.selectList(null);
        int teacherSize = teachers.size();
        //判断结果：
        int teamSize = 0;
        for (Teacher teacher : teachers) {
            teamSize += teacher.getMaxNum() - teacher.getCurrentNum();
        }
        int result ;
        if (teamSize > userSize) {
            result = 1;
            log.info("随机分配结果：{}", "学生全部分配完毕，老师队伍名额有多余");
        }else if (teamSize < userSize) {
            result = -1;
            log.info("随机分配结果：{}", "学生没有分配完毕，老师队伍名额没有了");

        }else {
            result = 0;
            log.info("随机分配结果：{}", "学生全部分配完毕，老师队伍名额无多余");

        }

        SecureRandom random = new SecureRandom();
        //随机加入
        while (userSize > 0) {
            for (Teacher teacher : teachers) {
                    int currentNum = teacher.getCurrentNum();
                    int maxNum = teacher.getMaxNum();
                    while (maxNum - currentNum > 0) {
                        if (userSize <= 0) {
                            return result;
                        }
                        int userIndex = random.nextInt(userSize);
                        if (users.get(userIndex).getUserRole() == 0) {
                            joinTeam(users.get(userIndex).getId(), teacher.getId(), currentNum++);
                        }
                        users.remove(userIndex);
                        userSize --;
                }
            }
            return result;

        }

        return result;
    }

    /**
     * 删除所有学生
     * @param request
     * @return
     */
    @Override
    public boolean deleteAllStudents(HttpServletRequest request) {
        //1. 鉴权
        checkRole(request);
        //2. 删除所有学生
        int delete = userMapper.delete(new QueryWrapper<User>().ne("userRole", 1));
        //3. 清除所有学生入队情况
        userTeamMapper.delete(null);
        //4. 归零老师队伍人数和申请人数
        teacherMapper.update(new UpdateWrapper<Teacher>().set("currentNum", 0).set("applyNum", 0));
        return delete != 0;
    }

    /**
     * 导出 excel 数据
     * @param request
     * @return
     */
    @Override
    public String exportExcel(HttpServletRequest request) {
        //1. 鉴权
        checkRole(request);
        //2. 定义要导出的数据
        List<ExportExcelData> exportData = new ArrayList<>();
        //3. 查询出所有老师 teachers
        List<Teacher> teachers = teacherMapper.selectList(null);
        //4. 遍历所有老师 teacher
        for (Teacher teacher : teachers) {
            List<UserTeam> userTeams = userTeamMapper.selectList(new QueryWrapper<UserTeam>().eq("teacherId", teacher.getId()));
            if (userTeams.isEmpty()) {
                continue;
            }
            List<UserTeam> joinedStudents = new ArrayList<>();
            List<UserTeam> appliedStudents = new ArrayList<>();
            for (UserTeam userTeam : userTeams) {
                if (userTeam.getIsJoin() == 1) {
                    joinedStudents.add(userTeam);
                } else {
                    appliedStudents.add(userTeam);
                }
            }
            // 遍历 joinedStudents
            for (UserTeam userTeam : joinedStudents) {
                // 查出单个学生信息
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userTeam.getUserId()));
                if (user == null) {
                    throw new IllegalArgumentException("学生信息不存在，数据库错误，请联系管理员！");
                }
                // 构建 exportExcelData 数据
                ExportExcelData data = getExportExcelData(teacher, user, "已加入");
                // 存入导出数据 exportData
                exportData.add(data);
            }
            // 遍历 appliedStudents
            for (UserTeam userTeam : appliedStudents) {
                // 查出单个学生信息
                User user = userMapper.selectOne(new QueryWrapper<User>().eq("id", userTeam.getUserId()));
                if (user == null) {
                    throw new IllegalArgumentException("学生信息不存在，数据库错误，请联系管理员！");
                }
                // 构建 exportExcelData 数据
                ExportExcelData data = getExportExcelData(teacher, user, "已申请");
                // 存入导出数据 exportData
                exportData.add(data);
            }
        }
        String fileName = UUID.randomUUID() + ".xlsx";
        // 判断 export 文件夹是否存在，不存在则创建
        File file = new File(exportUrl);
        if (!file.exists()) {
            boolean created = file.mkdir();
            if (created) {
                log.info("文件夹{}创建成功", exportUrl);
            } else {
                log.info("文件夹{}创建失败", exportUrl);
            }
        }
        EasyExcel.write(exportUrl + fileName, ExportExcelData.class).sheet("模板").doWrite(exportData);
        return "/download/export/" + fileName;
    }

    /**
     * 添加部分学生到队伍中
     * @param userIds
     * @param teacherId
     * @param request
     */
    @Override
    public void addStudentsToTeam(Long[] userIds, Long teacherId, HttpServletRequest request) {
        //1. 鉴权
        checkRole(request);
        //2. 参数校验
        if (userIds == null || userIds.length == 0 || teacherId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        List<Long> userIdList = Arrays.asList(userIds);
        List<User> users = userMapper.selectBatchIds(userIdList);
        if (users.size() != userIds.length) {
            throw new IllegalArgumentException("有学生不存在，请重试");
        }
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new IllegalArgumentException("教师队伍不存在，请重试");
        }

        //3. 判断学生是否入队
        users.forEach(user -> {
            UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", user.getId()));
            if (userTeam != null && userTeam.getIsJoin() == 1) {
                throw new IllegalArgumentException("存在学生已经入队，操作失败，请重试");
            }
        });

        //4. 判断教师队伍剩余名额，是否足够
        if (teacher.getMaxNum() - teacher.getCurrentNum() < users.size()) {
            throw new IllegalArgumentException("队伍剩余名额不足，无法加入，请重试");
        }

        //5. 入队
        users.forEach(user -> {
            UserTeam userTeam = new UserTeam();
            userTeam.setUserId(user.getId());
            userTeam.setTeacherId(teacherId);
            userTeam.setIsJoin(1);
            userTeamMapper.insert(userTeam);
        });

        //6. 减去老师队伍队伍名额
        teacherMapper.update(new UpdateWrapper<Teacher>().eq("id", teacherId).set("currentNum", teacher.getCurrentNum() + users.size()));

        //7. 删去所有学生在其他队伍的申请情况
        users.forEach(user -> {
            UserTeam userTeam = userTeamMapper.selectOne(new QueryWrapper<UserTeam>().eq("userId", user.getId()).eq("isJoin", 0));
            if (userTeam != null) {
                userTeamMapper.deleteById(userTeam.getId());
            }
        });
    }

    private ExportExcelData getExportExcelData(Teacher teacher, User user, String status) {
        ExportExcelData data = new ExportExcelData();
        data.setTeacherName(teacher.getName());
        data.setTeacherAccount(teacher.getUserAccount());
        data.setTeamNumber(teacher.getCurrentNum() + "/" + teacher.getMaxNum());
        data.setApplyNumber(teacher.getApplyNum() + "/" + teacher.getMaxApply());
        data.setStudentName(user.getUsername());
        data.setStudentAccount(user.getUserAccount());
        data.setStudentClass(user.getDegree());
        data.setStudentStatus(status);
        return data;
    }

    public void joinTeam(long userId, long teacherId, int currentNum) throws InterruptedException {
        //加入队伍
        UserTeam team = new UserTeam();
        team.setUserId(userId);
        team.setTeacherId(teacherId);
        team.setIsJoin(1);
        userTeamMapper.insert(team);
        //减少名额
        teacherMapper.update(new UpdateWrapper<Teacher>()
                .eq("id", teacherId)
                .set("currentNum", currentNum + 1));
    }

    public void checkRole(HttpServletRequest request) {
        User user = CheckLoginUtils.checkUserLoginStatus(request, redisCache);
        if (user.getUserRole() == 0) {
            throw new IllegalArgumentException("您没有权限，请重试");
        }
    }
 }
