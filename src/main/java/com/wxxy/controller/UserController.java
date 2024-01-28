package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.UserService;
import com.wxxy.service.impl.AuthServiceImpl;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        Teacher teacher = (Teacher) request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE);
        if (teacher == null) {
            throw new RuntimeException("您已退出，请重新登录");
        }
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
     * 查看我的队伍
     * @param request 获取老师id
     * @return
     */
    @GetMapping("/getMyJoinedStudent")
    public BaseResponse<List<User>> joinedStudent(HttpServletRequest request) {
        if (request.getSession().getAttribute(AuthServiceImpl.USER_LOGIN_STATE) == null) {
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
     * 更改队伍容量
     * @param maxSize 要修改的容量
     * @param request 获取老师信息
     * @return
     */
    @PostMapping("/changeMaxSize")
    public BaseResponse<Object> changeMaxSize(int maxSize, HttpServletRequest request) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("最大数量不能修改为小于0的数字");
        }
        boolean result = userService.changeMaxSize(maxSize, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "修改队伍容量成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "修改队伍容量失败");
    }
}
