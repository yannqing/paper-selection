package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.service.TeacherService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    /**
     * 查询全部老师信息
     * @param currentPage 当前页码
     * @param pageSize 一页的数据条数
     * @param request 获取session
     * @return
     */
    @GetMapping("/getAll")
    public BaseResponse<GetAllByPageVo<StudentGetTeachersVo>> getAllTeacher(Integer currentPage, Integer pageSize, HttpServletRequest request) {
        GetAllByPageVo<StudentGetTeachersVo> allTeachers = teacherService.getAllTeachers(currentPage,pageSize,request);
        return ResultUtils.success(Code.SUCCESS, allTeachers, "查询所有老师成功");
    }

    /**
     * 用户加入老师队伍
     * @param teacherIds 加入的老师id数组，最大2个
     * @param userId 登录的用户id，前面无session时写的，后面可以优化掉
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Teacher> joinTeacher(int[] teacherIds, Long userId) {
        boolean result = teacherService.joinTeacher(teacherIds, userId);
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
        return ResultUtils.success(Code.SUCCESS, count, "查询此用户申请的老师队伍数量");
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
    public BaseResponse<String> uploadAvatar(@RequestParam("avatar") MultipartFile avatar, HttpServletRequest request) throws IOException {
        String downloadUrl = teacherService.uploadAvatar(avatar, request);

        return ResultUtils.success(Code.SUCCESS, downloadUrl, "上传成功！");
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

    /**
     * 取消申请
     * @param teacherId 要取消申请的老师队伍id
     * @param request 获取session
     * @return
     */
    @PostMapping("/cancelApplication")
    public BaseResponse<Object> cancelApplication(Long teacherId, HttpServletRequest request) {
        if (teacherId == null) {
            throw new IllegalArgumentException("老师队伍id为空，无法取消");
        }
        boolean result = teacherService.cancelApplication(teacherId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "取消申请成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "取消申请失败");
    }

    /**
     * 查看队伍容量
     * @param request 获取session
     * @return
     */
    @GetMapping("/getCountOfTeam")
    public BaseResponse<CountOfTeamVo> getCountOfTeam(HttpServletRequest request) {
        CountOfTeamVo countOfTeam = teacherService.getCountOfTeam(request);
        return ResultUtils.success(Code.SUCCESS, countOfTeam, "查看队伍容量成功");
    }

    @GetMapping("/getMaxApply")
    public BaseResponse<Integer> getMaxApply(HttpServletRequest request) {
        return null;
    }


    /**
     * 获取个人信息（老师）
     * @param request 获取session
     * @return
     */
    @GetMapping("/getMyselfInfo")
    public BaseResponse<Teacher> getMyselfInfo(HttpServletRequest request) {
        Teacher myselfInfo = teacherService.getMyselfInfo(request);
        return ResultUtils.success(Code.SUCCESS, myselfInfo, "获取个人信息成功");
    }

    /**
     * 修改个人密码（老师）
     * @param oldPassword
     * @param newPassword
     * @param againPassword
     * @param request
     * @return
     */
    @PostMapping("/changeMyPassword")
    public BaseResponse<Object> changeMyPassword(@RequestParam("oldPassword") String oldPassword,
                                                 @RequestParam("newPassword") String newPassword,
                                                 @RequestParam("againPassword") String againPassword,
                                                 HttpServletRequest request) {
        boolean result = teacherService.changeMyPassword(oldPassword, newPassword, againPassword, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "修改老师密码成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "修改老师密码失败");
    }

    /**
     * 修改个人信息（老师）
     * @param updateTeacher
     * @param request
     * @return
     */
    @PostMapping("/updateMyselfInfo")
    public BaseResponse<Object> updateMyselfInfo(Teacher updateTeacher, HttpServletRequest request){
        boolean result = teacherService.updateMyselfInfo(updateTeacher, request);
        if (result) {
            log.info("修改老师个人信息成功");
            return ResultUtils.success(Code.SUCCESS, null, "修改老师个人信息成功！");
        }
        log.info("修改老师个人信息失败");
        return ResultUtils.failure(Code.FAILURE, null, "修改老师个人信息失败");
    }
}
