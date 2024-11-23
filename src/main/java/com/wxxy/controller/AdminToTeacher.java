package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
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
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

/**
 * @description: 管理员对教师的操作
 * @author: yannqing
 * @create: 2024-11-08 21:20
 * @from: <更多资料：yannqing.com>
 **/
@Slf4j
@RestController
@RequestMapping("/admin-teacher")
@Tag(name = "管理员操作教师接口")
public class AdminToTeacher {

    @Resource
    private AdminService adminService;

    /**
     * 新增教师
     * @param teacher 教师的基本信息
     * @return
     */
    @Operation(summary = "新增教师")
    @PostMapping("/addTeacher")
    public BaseResponse<Object> addTeacher(Teacher teacher, HttpServletRequest request){
        if (teacher == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        boolean result = adminService.addTeacher(teacher, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "新增教师成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "新增教师失败");
    }

    /**
     * 根据教师id删除教师
     * @param teacherId 教师id
     * @return
     */
    @Operation(summary = "根据教师id删除教师")
    @DeleteMapping("/deleteTeacher")
    public BaseResponse<Object> deleteTeacher(Long teacherId, HttpServletRequest request) {
        if (teacherId == null) {
            throw new IllegalArgumentException("教师id为空，无法删除");
        }
        boolean result = adminService.deleteTeacher(teacherId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "删除教师成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "删除教师失败");
    }

    /**
     * 上传excel文档，新增教师数据
     * @param file
     * @return uuid有需要覆盖的数据，null无
     * @throws IOException
     */
    @Operation(summary = "批量新增教师数据")
    @PostMapping("/uploadExcelTeacher")
    public BaseResponse<Object> uploadExcelTeacher(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        String result = adminService.uploadExcelTeacher(file, request);
        return ResultUtils.success(Code.SUCCESS, result, "新增excel数据成功");
    }

    /**
     * 查看老师的队伍
     * @param request
     * @return
     */
    @Operation(summary = "查看老师的队伍")
    @GetMapping("/getTeam")
    public BaseResponse<List<User>> joinedStudent(HttpServletRequest request, Integer teacherId) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new IllegalStateException("您已退出，请重新登录！");
        }
        List<User> joinedStudent = adminService.joinedStudent(request, teacherId);
        return ResultUtils.success(Code.SUCCESS, joinedStudent, "查看老师队伍成功");
    }

    /**
     * 重置教师密码
     * @param request
     * @param teacherId
     * @return
     */
    @Operation(summary = "重置教师密码")
    @PostMapping("/resetTeacherPassword")
    public BaseResponse<Object> resetTeacherPassword(HttpServletRequest request, Long teacherId) {
        boolean result = adminService.resetTeacherPassword(teacherId, request);
        if (result) {
            log.info("重置教师密码成功");
            return ResultUtils.success(Code.SUCCESS, null, "重置教师密码成功");
        }
        log.info("重置教师密码失败");
        return ResultUtils.failure(Code.FAILURE, null, "重置教师密码失败");
    }

    /**
     * 查询所有的教师
     * @return
     */
    @Operation(summary = "查询所有的教师")
    @GetMapping("/getAllTeachers")
    public BaseResponse<GetAllByPageVo<Teacher>> getAllTeachers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        return ResultUtils.success(Code.SUCCESS, adminService.getAllTeachers(currentPage, pageSize, searchAccount, request), "查询所有教师成功");
    }

    /**
     * 修改老师信息
     * @param teacher
     * @return
     */
    @Operation(summary = "修改老师信息")
    @PutMapping("/updateTeacher")
    public BaseResponse<Object> updateTeacher(Teacher teacher, HttpServletRequest request) {
        if (teacher == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        boolean result = adminService.updateTeacher(teacher, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "更新数据成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "更新数据失败");
    }

    /**
     * 修改所有老师队伍限制人数
     * @param teamSize
     * @param request
     * @return 如果是true，则全部修改成功，否则的话，队伍人数大于修改的限制，无法修改
     */
    @Operation(summary = "修改所有老师队伍限制人数")
    @PostMapping("/changeAllTeachersTeamSize")
    public BaseResponse<Object> changeAllTeachersTeamSize(Integer teamSize, HttpServletRequest request) {
        boolean result = adminService.changeAllTeachersTeamSize(teamSize, request);
        if (result) {
            return ResultUtils.success(Code.CHANGE_SIZE_SUCCESS, null, "修改所有老师的队伍人数限制成功！");
        } else {
            return ResultUtils.failure(Code.CHANGE_SIZE_FAILURE, null, "修改所有的老师队伍人数限制：存在修改失败的老师！");
        }
    }

    /**
     * 修改全部老师的队伍申请限制
     * @param applySize
     * @param request
     * @return 如果是true，则全部修改成功，否则的话，存在修改失败的老师
     */
    @Operation(summary = "修改全部老师的队伍申请限制")
    @PostMapping("/changeAllTeachersApplySize")
    public BaseResponse<Object> changeAllTeachersApplySize(Integer applySize, HttpServletRequest request) {
        boolean result = adminService.changeAllTeachersApplySize(applySize, request);
        if (result) {
            return ResultUtils.success(Code.CHANGE_SIZE_SUCCESS, null, "修改所有老师的队伍申请限制成功！");
        } else {
            return ResultUtils.failure(Code.CHANGE_SIZE_FAILURE, null, "修改所有的老师队伍申请限制：存在修改失败的老师！");
        }
    }

    /**
     * 更改队伍容量
     * @param maxSize 要修改的容量
     * @param teacherId 要修改的老师id
     * @param request 获取老师信息
     * @return
     */
    @Operation(summary = "更改队伍容量")
    @PostMapping("/changeMaxSize")
    public BaseResponse<Object> changeMaxSize(int maxSize, int teacherId, HttpServletRequest request) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("最大数量不能修改为小于0的数字");
        }
        boolean result = adminService.changeMaxSize(maxSize, teacherId, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "修改队伍容量成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "修改队伍容量失败");
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
     * 更改申请容量
     * @param applySize 最新申请限制数量
     * @param teacherId 要修改的老师id
     * @param request 验证登录
     * @return
     */
    @Operation(summary = "更改申请容量")
    @PostMapping("/changeApplySize")
    public BaseResponse<Object> changeApplySize(int applySize, int teacherId, HttpServletRequest request) {
        //查询参数是否合法
        if (applySize <= 0) {
            throw new IllegalArgumentException("参数不合法，申请容量不能<=0");
        }
        boolean result = adminService.changeApplySize(applySize, teacherId, request);
        return ResultUtils.success(Code.SUCCESS, result, "修改队伍的申请限制成功！");
    }
}
