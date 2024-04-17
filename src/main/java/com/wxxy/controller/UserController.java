package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.User;
import com.wxxy.service.UserService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/student")
public class UserController {

    @Resource
    private UserService userService;
    /**
     * 查询已选择的学生
     * @param request 获取session
     * @return
     */
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

    /**
     * 获取个人信息（学生）
     * @param request 获取session
     * @return
     */
    @GetMapping("/getMyselfInfo")
    public BaseResponse<User> getMyselfInfo(HttpServletRequest request) {
        User myselfInfo = userService.getMyselfInfo(request);
        return ResultUtils.success(Code.SUCCESS, myselfInfo, "获取个人信息成功");
    }

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
}
