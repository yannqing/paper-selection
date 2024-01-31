package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.AdminService;
import com.wxxy.service.UserService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

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
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());
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
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + teacher.getUserPassword()).getBytes());
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

    @Override
    public boolean updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", user.getId());
        updateWrapper.set("username", user.getUsername());
        updateWrapper.set("academy", user.getAcademy());
        updateWrapper.set("degree", user.getDegree());
        updateWrapper.set("userAccount", user.getUserAccount());
        updateWrapper.set("userPassword", DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes()));
        updateWrapper.set("gender", user.getGender());
        updateWrapper.set("profile", user.getProfile());
        updateWrapper.set("phone", user.getPhone());
        updateWrapper.set("email", user.getEmail());
        updateWrapper.set("userStatus", user.getUserStatus());
        int result = userMapper.update(null, updateWrapper);
        return result == 1;
    }

    @Override
    public boolean updateTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UpdateWrapper<Teacher> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", teacher.getId());
        updateWrapper.set("name", teacher.getName());
        updateWrapper.set("userAccount", teacher.getUserAccount());
        updateWrapper.set("userPassword", DigestUtils.md5DigestAsHex((SALT + teacher.getUserPassword()).getBytes()));
        updateWrapper.set("avatarUrl", teacher.getAvatarUrl());
        updateWrapper.set("description", teacher.getDescription());
        updateWrapper.set("phone", teacher.getPhone());
        updateWrapper.set("email", teacher.getEmail());
        updateWrapper.set("maxNum", teacher.getMaxNum());
        int result = teacherMapper.update(null, updateWrapper);
        return result == 1;
    }

    @Override
    public boolean uploadExcelStudent(MultipartFile studentExcel) throws IOException {
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
                // 遍历行中的单元格
                Iterator<Cell> cellIterator = row.cellIterator();
                int cellIndex = 0 ;
                User user = new User();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (cell.getCellType() == CellType.NUMERIC) {
                        // 将NUMERIC类型的单元格转换为STRING类型
                        cell.setCellType(CellType.STRING);
                    }
                    String cellValue = cell.getStringCellValue();
                    switch (cellIndex) {
                        case 0: user.setUsername(cellValue); break;
                        case 1: user.setAcademy(cellValue); break;
                        case 2: user.setDegree(cellValue); break;
                        case 3: user.setUserAccount(cellValue); break;
                        case 4: user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes())); break;
                        case 5: user.setGender(cellValue.equals("男")?1:0); break;
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
            for (User user : users) {
                System.out.println(user);
                userMapper.insert(user);
            }
            return true;
    }

    @Override
    public boolean uploadExcelTeacher(MultipartFile teacherExcel) throws IOException {
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
            // 遍历行中的单元格
            Iterator<Cell> cellIterator = row.cellIterator();
            int cellIndex = 0 ;
            Teacher teacher = new Teacher();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType() == CellType.NUMERIC) {
                    // 将NUMERIC类型的单元格转换为STRING类型
                    cell.setCellType(CellType.STRING);
                }
                String cellValue = cell.getStringCellValue();
                switch (cellIndex) {
                    case 0: teacher.setName(cellValue); break;
                    case 1: teacher.setUserAccount(cellValue); break;
                    case 2: teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + cellValue).getBytes())); break;
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
        for (Teacher teacher : teachers) {
            System.out.println(teacher);
            teacherMapper.insert(teacher);
        }
        return true;

    }
}
