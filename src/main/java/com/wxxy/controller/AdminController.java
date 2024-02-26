package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.AdminService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import com.wxxy.vo.GetAllByPageVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.ResultType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

@RestController
@RequestMapping("/admin")
public class AdminController {

//TODO 这里的所有接口要经过权限校验
    @Resource
    private AdminService adminService;

    /**
     * 新增学生
     * @param user 学生的基本信息
     * @return
     */
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
     * 新增教师
     * @param teacher 教师的基本信息
     * @return
     */
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
     * 根据学生id删除学生
     * @param userId 学生id
     * @return
     */
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
     * 根据教师id删除教师
     * @param teacherId 教师id
     * @return
     */
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
     * 查询未加入队伍的学生
     * @return
     */
    @GetMapping("/getUsersUnselecting")
    public BaseResponse<GetAllByPageVo<User>> getUsersUnselecting(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request){
        GetAllByPageVo<User> usersUnselecting = adminService.getUsersUnselecting(currentPage, pageSize, searchAccount, request);
        return ResultUtils.success(Code.SUCCESS, usersUnselecting, "查看未加入队伍的学生成功");
    }

    /**
     * 查询所有的学生
     * @return
     */
    @GetMapping("/getAllUsers")
    public BaseResponse<GetAllByPageVo<User>> getAllUsers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        return ResultUtils.success(Code.SUCCESS, adminService.getAllUsers(currentPage, pageSize, searchAccount, request), "查询所有学生成功");
    }

    /**
     * 查询所有的教师
     * @return
     */
    @GetMapping("/getAllTeachers")
    public BaseResponse<GetAllByPageVo<Teacher>> getAllTeachers(Integer currentPage, Integer pageSize, String searchAccount, HttpServletRequest request) {
        return ResultUtils.success(Code.SUCCESS, adminService.getAllTeachers(currentPage, pageSize, searchAccount, request), "查询所有教师成功");
    }
    // 修改

    /**
     * 修改学生信息
     * @param user
     * @return
     */
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
     * 修改老师信息
     * @param teacher
     * @return
     */
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
     * 上传excel文档，新增学生数据
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadExcelStudent")
    public BaseResponse<Object> uploadExcelStudent(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        boolean result = adminService.uploadExcelStudent(file, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "新增excel数据成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "新增excel数据失败");
    }

    /**
     * 上传excel文档，新增教师数据
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadExcelTeacher")
    public BaseResponse<Object> uploadExcelTeacher(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        boolean result = adminService.uploadExcelTeacher(file, request);
        if (result) {
            return ResultUtils.success(Code.SUCCESS, null, "新增excel数据成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "新增excel数据失败");
    }

    /**
     * 查看老师的队伍
     * @param request
     * @return
     */
    @GetMapping("/getTeam")
    public BaseResponse<List<User>> joinedStudent(HttpServletRequest request, Integer teacherId) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new IllegalStateException("您已退出，请重新登录！");
        }
        List<User> joinedStudent = adminService.joinedStudent(request, teacherId);
        return ResultUtils.success(Code.SUCCESS, joinedStudent, "查看老师队伍成功");
    }
}
