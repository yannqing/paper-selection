package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.TeacherService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.JoinTeacherVo;
import com.wxxy.vo.JoinedTeacherStatusVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    /**
     * 查询全部老师信息
     * @return
     */
    @GetMapping("/getAll")
    public BaseResponse<List<Teacher>> getAllTeacher() {
        List<Teacher> allTeachers = teacherService.getAllTeachers();
        return ResultUtils.success(Code.SUCCESS, allTeachers, "查询所有老师成功");
    }

    /**
     * 用户加入老师队伍
     * @param joinTeacherVo
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Teacher> joinTeacher(@RequestBody JoinTeacherVo joinTeacherVo) {
        boolean result = teacherService.joinTeacher(joinTeacherVo.getTeacherIds(), joinTeacherVo.getUserId());
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "申请加入队伍成功，在审核");
        }
        return ResultUtils.failure(Code.FAILURE, null, "申请加入队伍失败");
    }

    /**
     * 查询当前用户已选择的老师数目
     * @return
     */
    @GetMapping("/getAccount")
    public BaseResponse<Integer> getAccount(Long userId) {
        int count = teacherService.selectedTeacherAccount(userId);
        return ResultUtils.success(Code.SUCCESS, count, "查询此用户加入的老师队伍数量");
    }

    /**
     * 返回用户加入的所有队伍名称和状态
     * @param userId
     * @return
     */
    @GetMapping("/getJoinedTeacherStatus")
    public BaseResponse<List<JoinedTeacherStatusVo>> getJoinedTeacherStatus(Long userId) {
        List<JoinedTeacherStatusVo> joinedTeacherStatus = teacherService.getJoinedTeacherStatus(userId);
        return ResultUtils.success(Code.SUCCESS, joinedTeacherStatus, "查询此用户加入的所有队伍名称，状态成功");
    }


    @PostMapping("/uploadAvatar")
    public BaseResponse<Boolean> uploadAvatar(@RequestParam("avatar") MultipartFile avatar, HttpServletRequest request) throws JsonProcessingException {
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar cannot be null");
        }

        return null;
    }

    /**
     * 退出队伍
     * @param teacherId 要退出的队伍id
     * @param request 获取session
     * @return 返回退出结果
     */
    @PostMapping("/exitTeam")
    public BaseResponse<Boolean> exitTeam(Long teacherId, HttpServletRequest request) {
        //校验teacherId是否合法
        if (teacherId == null) {
            throw new IllegalArgumentException("老师id不能为空");
        }
        if (teacherService.getById(teacherId) == null) {
            throw new IllegalArgumentException("此老师不存在");
        }
        boolean result = teacherService.exitTeam(teacherId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "退出队伍成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "退出队伍失败");
    }
}
