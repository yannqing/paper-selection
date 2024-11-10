package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.JoinedTeacherStatusVo;
import com.wxxy.vo.StudentGetTeachersVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/student")
@Tag(name = "学生接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private TeacherService teacherService;

    /**
     * 获取个人信息（学生）
     * @param request 获取session
     * @return
     */
    @Operation(summary = "获取个人信息（学生）")
    @GetMapping("/getMyselfInfo")
    public BaseResponse<User> getMyselfInfo(HttpServletRequest request) {
        User myselfInfo = userService.getMyselfInfo(request);
        return ResultUtils.success(Code.SUCCESS, myselfInfo, "获取个人信息成功");
    }

    /**
     * 修改密码（学生）
     * @param oldPassword
     * @param newPassword
     * @param againPassword
     * @param request
     * @return
     */
    @Operation(summary = "修改密码（学生）")
    @PostMapping("/changeMyPassword")
    public BaseResponse<Object> changeMyPassword(@RequestParam("oldPassword") String oldPassword,
                                                 @RequestParam("newPassword") String newPassword,
                                                 @RequestParam("againPassword") String againPassword,
                                                 HttpServletRequest request) {
        boolean result = userService.changeMyPassword(oldPassword, newPassword, againPassword, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "修改学生密码成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "修改学生密码失败");
    }

    /**
     * 修改个人信息（学生）
     * @param updateUser
     * @param request
     * @return
     */
    @Operation(summary = "修改个人信息（学生）")
    @PostMapping("/updateMyselfInfo")
    public BaseResponse<Object> updateMyselfInfo(User updateUser, HttpServletRequest request){
        boolean result = userService.updateMyselfInfo(updateUser, request);
        if (result) {
            log.info("修改学生个人信息成功");
            return ResultUtils.success(Code.SUCCESS, null, "修改学生个人信息成功！");
        }
        log.info("修改学生个人信息失败");
        return ResultUtils.failure(Code.FAILURE, null, "修改学生个人信息失败");
    }

    /**
     * 查询全部老师信息
     * @param currentPage 当前页码
     * @param pageSize 一页的数据条数
     * @param request 获取session
     * @return
     */
    @Operation(summary = "查询全部老师信息")
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
    @Operation(summary = "学生加入老师队伍")
    @PostMapping("/join")
    public BaseResponse<Teacher> joinTeacher(Integer teacherIds, Long userId, HttpServletRequest request) {
        boolean result = teacherService.joinTeacher(teacherIds, userId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "申请加入队伍成功，在审核");
        }
        return ResultUtils.failure(Code.FAILURE, null, "申请加入队伍失败");
    }

    /**
     * 查询当前用户已选择的老师数目
     * @return
     */
    @Operation(summary = "查询当前用户已选择的老师数目")
    @GetMapping("/getAccount")
    public BaseResponse<Integer> getAccount(Long userId, HttpServletRequest request) {
        int count = teacherService.selectedTeacherAccount(userId, request);
        return ResultUtils.success(Code.SUCCESS, count, "查询此用户申请的老师队伍数量");
    }

    /**
     * 返回用户加入的所有队伍名称和状态
     * @param userId
     * @return
     */
    @Operation(summary = "返回用户加入的所有队伍名称和状态")
    @GetMapping("/getJoinedTeacherStatus")
    public BaseResponse<List<JoinedTeacherStatusVo>> getJoinedTeacherStatus(Long userId, HttpServletRequest request) {
        List<JoinedTeacherStatusVo> joinedTeacherStatus = teacherService.getJoinedTeacherStatus(userId, request);
        return ResultUtils.success(Code.SUCCESS, joinedTeacherStatus, "查询此用户加入的所有队伍名称，状态成功");
    }

    /**
     * 退出队伍
     * @param teacherId 要退出的队伍id
     * @param request 获取session
     * @return 返回退出结果
     */
    @Operation(summary = "退出队伍")
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
    @Operation(summary = "取消申请")
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
}
