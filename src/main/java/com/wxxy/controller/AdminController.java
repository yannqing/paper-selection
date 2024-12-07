package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxxy.common.Code;
import com.wxxy.service.AdminService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员接口")
public class AdminController {

    @Resource
    private AdminService adminService;

    /**
     * 覆盖Excel数据
     * @param isCover uuid为覆盖，null为不覆盖
     * @param role 1学生，0老师
     * @param request session
     * @return 返回封装
     */
    @Operation(summary = "覆盖Excel数据")
    @PostMapping("/cover")
    public BaseResponse<Object> coverExcel(String isCover, int role, HttpServletRequest request) throws JsonProcessingException {
        adminService.isCover(isCover, role, request);
        return ResultUtils.success(Code.SUCCESS, null, "覆盖成功");
    }

    /**
     * 导出Excel数据
     * @param request session
     * @return 返回封装
     */
    @Operation(summary = "导出Excel数据")
    @PostMapping("/export")
    public BaseResponse<Object> exportExcel(HttpServletRequest request) {
        String fileName = adminService.exportExcel(request);
        return ResultUtils.success(Code.SUCCESS, fileName, "导出数据成功");
    }

    /**
     * 添加部分学生到队伍中
     * @param userIds 要添加的学生
     * @param teacherId 要加入的队伍是
     * @param request session
     * @return 返回封装
     */
    @Operation(summary = "添加部分学生到队伍中")
    @PostMapping("/addStudentsToTeam")
    public BaseResponse<Object> addStudentsToTeam(Long[] userIds, Long teacherId, HttpServletRequest request) {
        adminService.addStudentsToTeam(userIds, teacherId, request);
        return ResultUtils.success(Code.SUCCESS, null, "添加成功");
    }
}
