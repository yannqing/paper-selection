package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxxy.common.DateFormat;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.AdminService;
import com.wxxy.utils.CheckLoginUtils;
import com.wxxy.vo.GetAllByPageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static com.wxxy.common.UserLoginState.SALT;
import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;
import static com.wxxy.utils.CheckLoginUtils.checkTeacherLoginStatus;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;

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
        //删除
        int result = userMapper.delete(new QueryWrapper<User>().eq("id", userId));
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
            userPage = userMapper.selectPage(pageConfig, null);
        } else {
            QueryWrapper<User> query = new QueryWrapper<>();
            query.like("userAccount", searchAccount);
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
        int result = teacherMapper.update(null, updateWrapper);
        return result == 1;
    }

    @Override
    public boolean uploadExcelStudent(MultipartFile studentExcel, HttpServletRequest request) throws IOException {
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
                        case 4: {
                            if (cellValue != null) {
                                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes()));
                            }else {
                                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
                            }
                            break;
                        }
                        case 5: {
                            if (cellValue != null) {
                                user.setGender(cellValue.equals("男")?1:0);
                            }else{
                                user.setGender(1);
                            }
                            break;
                        }
                        case 6: user.setProfile(cellValue); break;
                        case 7: user.setPhone(cellValue); break;
                        case 8: user.setEmail(cellValue);
                    }
//                    rowData.add(cellValue);
                    cellIndex++;
                }
                users.add(user);
//                data.add(rowData);
                rowIndex ++;
            }
            workbook.close();
//            System.out.println(data);
            //查重
            for (User user : users) {
                if (!userMapper.exists(new QueryWrapper<User>().eq("userAccount", user.getUserAccount()))){
                    userMapper.insert(user);
                }
            }
            return true;
    }

    @Override
    public boolean uploadExcelTeacher(MultipartFile teacherExcel, HttpServletRequest request) throws IOException {
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
                    case 2: {
                        if (cellValue != null) {
                            teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes()));
                        }else {
                            teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
                        }
                        break;
                    }
                    case 3: teacher.setDescription(cellValue); break;
                    case 4: teacher.setPhone(cellValue); break;
                    case 5: teacher.setEmail(cellValue);
                }
//                    rowData.add(cellValue);
                cellIndex++;
            }
            teachers.add(teacher);
//                data.add(rowData);
            rowIndex ++;
        }
        workbook.close();
//            System.out.println(data);
        //查重
        for (Teacher teacher : teachers) {
            System.out.println(teacher);
            if (!teacherMapper.exists(new QueryWrapper<Teacher>().eq("userAccount", teacher.getUserAccount()))){
                teacherMapper.insert(teacher);
            }
        }
        return true;

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


    public void checkRole(HttpServletRequest request) {
        User user = CheckLoginUtils.checkUserLoginStatus(request);
        if (user.getUserRole() == 0) {
            throw new IllegalArgumentException("您没有权限，请重试");
        }
    }
 }
