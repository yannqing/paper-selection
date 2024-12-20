package com.wxxy.service;

import com.wxxy.domain.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxxy.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
* @author 67121
* @description 针对表【teacher(老师(队伍))】的数据库操作Service
* @createDate 2024-01-24 00:24:30
*/
public interface TeacherService extends IService<Teacher> {

    GetAllByPageVo<StudentGetTeachersVo> getAllTeachers(Integer currentPage, Integer pageSize, HttpServletRequest request);

    boolean joinTeacher(Integer teachers, Long userId, HttpServletRequest request);

    int selectedTeacherAccount(Long userId, HttpServletRequest request);

    List<JoinedTeacherStatusVo> getJoinedTeacherStatus(Long userId, HttpServletRequest request);

    String uploadAvatar(MultipartFile avatar, HttpServletRequest request) throws IOException;

    boolean exitTeam(Long teacherId, HttpServletRequest request);

    boolean cancelApplication(Long teacherId, HttpServletRequest request);

    CountOfTeamVo getCountOfTeam(HttpServletRequest request);

//    Integer getMaxApply(HttpServletRequest request);

    Teacher getMyselfInfo(HttpServletRequest request);

    boolean changeMyPassword(String oldPassword, String newPassword, String againPassword, HttpServletRequest request);

    boolean updateMyselfInfo(Teacher updateTeacher, HttpServletRequest request);

    boolean changeApplySize(Integer applySize, HttpServletRequest request);

    boolean changeTeamMaxSize(Integer teamMaxNum, HttpServletRequest request);
}
