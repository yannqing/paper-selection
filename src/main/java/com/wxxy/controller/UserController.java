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
    @GetMapping("/selectedStudent")
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
}
