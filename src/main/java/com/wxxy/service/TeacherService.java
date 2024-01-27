package com.wxxy.service;

import com.wxxy.domain.Teacher;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxxy.vo.JoinedTeacherStatusVo;
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

    List<Teacher> getAllTeachers();

    boolean joinTeacher(int[] teachers, Long userId);

    int selectedTeacherAccount(Long userId);

    List<JoinedTeacherStatusVo> getJoinedTeacherStatus(Long userId);

    boolean uploadAvatar(MultipartFile avatar, HttpServletRequest request) throws IOException;

}
