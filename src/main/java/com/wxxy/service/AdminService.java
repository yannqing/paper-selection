package com.wxxy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.vo.GetAllByPageVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    boolean addUser(User user, HttpServletRequest request);

    boolean addTeacher(Teacher teacher, HttpServletRequest request);

    boolean deleteUser(Long userId, HttpServletRequest request);

    boolean deleteTeacher(Long teacherId, HttpServletRequest request);

    GetAllByPageVo<User> getAllUsers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request);

    GetAllByPageVo<Teacher> getAllTeachers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request);

    GetAllByPageVo<User> getUsersUnselecting(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request);

    boolean updateUser(User user, HttpServletRequest request);

    boolean updateTeacher(Teacher teacher, HttpServletRequest request);

    String uploadExcelStudent(MultipartFile studentExcel, HttpServletRequest request) throws IOException;

    String uploadExcelTeacher(MultipartFile teacherExcel, HttpServletRequest request) throws IOException;

    List<User> joinedStudent(HttpServletRequest request, Integer teacherId);

    boolean resetStudentPassword(Long userId, HttpServletRequest request);

    boolean resetTeacherPassword(Long teacherId, HttpServletRequest request);

    void isCover(String isCover, int role, HttpServletRequest request) throws JsonProcessingException;

    boolean changeAllTeachersTeamSize(Integer teamSize, HttpServletRequest request);

    boolean changeAllTeachersApplySize(Integer applySize, HttpServletRequest request);

    boolean changeMaxSize(int maxSize, int teacherId, HttpServletRequest request);

    boolean changeApplySize(int applySize, int teacherId, HttpServletRequest request);

    boolean removeFromTeam(Long userId, Long teacherId, HttpServletRequest request);

}
