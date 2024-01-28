package com.wxxy.controller;

import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.vo.BaseResponse;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // 新增 TODO excel导入
    @PostMapping("/addUser")
    public BaseResponse<Object> addUser(){
        return null;
    }
    @PostMapping("/addTeacher")
    public BaseResponse<Object> addTeacher(){
        return null;
    }
    // 删除
    @DeleteMapping("/deleteUser")
    public BaseResponse<Object> deleteUser() {
        return null;
    }
    @DeleteMapping("/deleteTeacher")
    public BaseResponse<Object> deleteTeacher() {
        return null;
    }
    // 查询 TODO 查看未加入队伍的学生
    @GetMapping("/getAllUsers")
    public BaseResponse<List<User>> getAllUsers() {
        return null;
    }

    @GetMapping("/getAllTeachers")
    public BaseResponse<List<Teacher>> getAllTeachers() {
        return null;
    }
    // 修改
    @PutMapping("/updateUser")
    public BaseResponse<Object> updateUser(){
        return null;
    }
    @PutMapping("/updateTeacher")
    public BaseResponse<Object> updateTeacher() {
        return null;
    }


}
