package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/teacher")
@Tag(name = "老师接口")
public class TeacherController {

    @Resource
    private TeacherService teacherService;

    @Resource
    private UserService userService;


    /**
     * 上传头像接口
     * @param avatar 头像文件
     * @param request 请求会话
     * @return
     * @throws IOException
     */
    @Operation(summary = "上传头像")
    @PostMapping("/uploadAvatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("avatar") MultipartFile avatar, HttpServletRequest request) throws IOException {
        String downloadUrl = teacherService.uploadAvatar(avatar, request);

        return ResultUtils.success(Code.SUCCESS, downloadUrl, "上传成功！");
    }

    /**
     * 查看队伍容量
     * @param request 获取session
     * @return
     */
    @Operation(summary = "查询队伍人数")
    @GetMapping("/getCountOfTeam")
    public BaseResponse<CountOfTeamVo> getCountOfTeam(HttpServletRequest request) {
        CountOfTeamVo countOfTeam = teacherService.getCountOfTeam(request);
        return ResultUtils.success(Code.SUCCESS, countOfTeam, "查看队伍容量成功");
    }

//    @GetMapping("/getMaxApply")
//    public BaseResponse<Integer> getMaxApply(HttpServletRequest request) {
//        return null;
//    }


    /**
     * 获取个人信息（老师）
     * @param request 获取session
     * @return
     */
    @Operation(summary = "获取个人信息（老师）")
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
    @Operation(summary = "修改个人密码（老师）")
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
    @Operation(summary = "修改个人信息（老师）")
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

    /**
     * 查询已选择的学生
     * @param request 获取session
     * @return
     */
    @Operation(summary = "查询已选择的学生")
    @GetMapping("/getMySelectedStudent")
    public BaseResponse<List<User>> getSelectedStudent(HttpServletRequest request){
        List<User> selectedStudent = userService.getSelectedStudent(request);
        return ResultUtils.success(Code.SUCCESS, selectedStudent, "查询已选择的学生成功");
    }

    /**
     * 同意学生加入队伍
     * @param userId 学生id
     * @param request 获取老师id
     * @return
     */
    @Operation(summary = "同意学生加入队伍")
    @PostMapping("/agreeJoin")
    public BaseResponse<Object> agreeJoin(Long userId, HttpServletRequest request){
        if (userId == null) {
            throw new IllegalArgumentException("学生id为空");
        }
        boolean result = userService.agreeJoin(userId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "用户加入队伍成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "用户加入队伍失败");
    }

    /**
     * 拒绝学生的申请
     * @param userId
     * @param request
     * @return
     */
    @Operation(summary = "拒绝学生的申请")
    @PostMapping("/disagreeJoin")
    public BaseResponse<Object> disagreeJoin(Long userId, HttpServletRequest request){
        if (userId == null) {
            throw new IllegalArgumentException("学生id为空");
        }
        boolean result = userService.disagreeJoin(userId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "已拒绝学生加入");
        }
        return ResultUtils.failure(Code.FAILURE, null, "拒绝学生加入失败");
    }

    /**
     * 查看我的队伍
     * @param request 获取老师id
     * @return
     */
    @Operation(summary = "查看我的队伍")
    @GetMapping("/getMyJoinedStudent")
    public BaseResponse<List<User>> joinedStudent(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new IllegalArgumentException("此老师已退出，请重新登录");
        }
        List<User> joinedStudent = userService.joinedStudent(request);
        return ResultUtils.success(Code.SUCCESS, joinedStudent, "查看我的队伍成功");
    }

    /**
     * 移出队伍
     * @param userId 要移出的用户id
     * @param request
     * @return
     */
    @Operation(summary = "移出队伍")
    @PostMapping("/removeFromTeam")
    public BaseResponse<Object> removeFromTeam(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("用户id不能未空");
        }
        boolean result = userService.removeFromTeam(userId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "移出队伍成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "移出队伍失败");
    }


}
