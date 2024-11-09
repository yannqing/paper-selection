package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.User;
import com.wxxy.service.AdminService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.GetAllByPageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @description: 管理员对学生的操作
 * @author: yannqing
 * @create: 2024-11-08 21:20
 * @from: <更多资料：yannqing.com>
 **/
@Slf4j
@RestController
@RequestMapping("/admin-student")
@Tag(name = "管理员操作学生接口")
public class AdminToStudent {

    @Resource
    private AdminService adminService;

    /**
     * 新增学生
     * @param user 学生的基本信息
     * @return
     */
    @Operation(summary = "新增学生")
    @PostMapping("/addUser")
    public BaseResponse<Object> addUser(User user, HttpServletRequest request){
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        boolean result = adminService.addUser(user, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null , "新增学生成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "新增学生失败");
    }

    /**
     * 根据学生id删除学生
     * @param userId 学生id
     * @return
     */
    @Operation(summary = "根据学生id删除学生")
    @DeleteMapping("/deleteUser")
    public BaseResponse<Object> deleteUser(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("学生id为空，无法删除");
        }
        boolean result = adminService.deleteUser(userId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "删除用户成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "删除用户失败");
    }

    /**
     * 查询未加入队伍的学生
     * @return
     */
    @Operation(summary = "查询未加入队伍的学生")
    @GetMapping("/getUsersUnselecting")
    public BaseResponse<GetAllByPageVo<User>> getUsersUnselecting(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request){
        GetAllByPageVo<User> usersUnselecting = adminService.getUsersUnselecting(currentPage, pageSize, searchAccount, request);
        return ResultUtils.success(Code.SUCCESS, usersUnselecting, "查看未加入队伍的学生成功");
    }

    /**
     * 查询所有的学生
     * @return
     */
    @Operation(summary = "查询所有的学生")
    @GetMapping("/getAllUsers")
    public BaseResponse<GetAllByPageVo<User>> getAllUsers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        return ResultUtils.success(Code.SUCCESS, adminService.getAllUsers(currentPage, pageSize, searchAccount, request), "查询所有学生成功");
    }

    /**
     * 修改学生信息
     * @param user
     * @return
     */
    @Operation(summary = "修改学生信息")
    @PutMapping("/updateUser")
    public BaseResponse<Object> updateUser(User user, HttpServletRequest request) {
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        boolean result = adminService.updateUser(user, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "更新数据成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "更新数据失败");
    }

    /**
     * 上传excel文档，新增学生数据
     * @param file
     * @return uuid有需要覆盖的数据，null无
     * @throws IOException
     */
    @Operation(summary = "批量新增学生数据")
    @PostMapping("/uploadExcelStudent")
    public BaseResponse<Object> uploadExcelStudent(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        String result = adminService.uploadExcelStudent(file, request);
        return ResultUtils.success(Code.SUCCESS, result, "新增excel数据成功");
    }

    /**
     * 重置学生密码
     * @param request
     * @param userId
     * @return
     */
    @Operation(summary = "重置学生密码")
    @PostMapping("/resetStudentPassword")
    public BaseResponse<Object> resetStudentPassword(HttpServletRequest request, Long userId) {
        boolean result = adminService.resetStudentPassword(userId, request);
        if (result) {
            log.info("重置学生密码成功");
            return ResultUtils.success(Code.SUCCESS, null, "重置学生密码成功");
        }
        log.info("重置学生密码失败");
        return ResultUtils.failure(Code.FAILURE, null, "重置学生密码失败");
    }

    /**
     * 移出队伍
     * @param userId 要移出的用户id
     * @param teacherId 要移出的队伍
     * @param request
     * @return
     */
    @Operation(summary = "移出队伍")
    @PostMapping("/removeFromTeam")
    public BaseResponse<Object> removeFromTeam(Long userId, Long teacherId, HttpServletRequest request) {
        if (userId == null || teacherId == null) {
            throw new IllegalArgumentException("参数不能未空");
        }
        boolean result = adminService.removeFromTeam(userId, teacherId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "管理员删除队伍（id：" + teacherId + "）中的成员（id：" + userId + "）成功！");
        }
        return ResultUtils.failure(Code.FAILURE, null, "管理员删除队伍（id：" + teacherId + "）中的成员（id：" + userId + "）失败！");
    }

    /**
     * 第三阶段：管理员随机分配所有名额
     * @param request
     * @return
     * @throws InterruptedException
     */
    @Operation(summary = "随机分配")
    @PostMapping("/distribute")
    public BaseResponse<Object> distribute(HttpServletRequest request) throws InterruptedException {
        Integer result = adminService.distribute(request);
        if (result == 1) {
            return ResultUtils.success(Code.DISTRIBUTE_TEACHER_REMAINING, null, "队伍名额有剩余，学生全部分配成功");
        }else if (result == 0) {
            return ResultUtils.success(Code.DISTRIBUTE_SUCCESS, null, "学生全部分配成功，所有老师队伍均已满");
        } else {
            return ResultUtils.success(Code.DISTRIBUTE_STUDENT_REMAINING, null, "仍有剩余学生没有分配成功，所有老师队伍均已满");
        }
    }
}
