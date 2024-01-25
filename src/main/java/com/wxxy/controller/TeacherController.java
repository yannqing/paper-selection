package com.wxxy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.service.TeacherService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.JoinTeacherVo;
import com.wxxy.vo.JoinedTeacherStatusVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public BaseResponse<Boolean> uploadAvatar(String userId, String avatar) {
        return null;
    }
}
