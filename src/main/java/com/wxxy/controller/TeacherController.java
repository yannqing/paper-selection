package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.service.TeacherService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @GetMapping("/getAll")
    public BaseResponse<List<Teacher>> getAllTeacher() {
        List<Teacher> allTeachers = teacherService.getAllTeachers();
        return ResultUtils.success(Code.SUCCESS, allTeachers, "查询所有老师成功");
    }

    /**
     * 用户加入老师队伍 TODO 待测试
     * @param teacherIds
     * @param userId
     * @return
     */
    @PostMapping("/join")

    public BaseResponse<Teacher> joinTeacher(@RequestBody int[] teacherIds, Long userId){
        boolean result = teacherService.joinTeacher(teacherIds, userId);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "加入队伍成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "加入队伍失败");
    }

    /**
     * 查询当前用户已选择的老师数目 TODO 待测试
     * @return
     */
    @GetMapping("/getAccount")
    public BaseResponse<Integer> getAccount(@RequestBody Long userId) {
        int count = teacherService.selectedTeacherAccount(userId);
        return ResultUtils.success(Code.SUCCESS, count, "查询此用户加入的老师队伍数量");
    }
}
