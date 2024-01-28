package com.wxxy.service;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;

import java.util.List;

public interface AdminService {
    boolean addUser(User user);

    boolean addTeacher(Teacher teacher);

    boolean deleteUser(Long userId);

    boolean deleteTeacher(Long teacherId);

    List<User> getAllUsers();

    List<Teacher> getAllTeachers();

    List<User> getUsersUnselecting();
}
