package com.wxxy.service;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    boolean addUser(User user);

    boolean addTeacher(Teacher teacher);

    boolean deleteUser(Long userId);

    boolean deleteTeacher(Long teacherId);

    List<User> getAllUsers();

    List<Teacher> getAllTeachers();

    List<User> getUsersUnselecting();

    boolean updateUser(User user);

    boolean updateTeacher(Teacher teacher);

    boolean uploadExcelStudent(MultipartFile studentExcel) throws IOException;

    boolean uploadExcelTeacher(MultipartFile teacherExcel) throws IOException;
}
