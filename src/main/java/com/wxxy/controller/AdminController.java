package com.wxxy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.ibatis.annotations.ResultType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.wxxy.common.UserLoginState.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员接口")
public class AdminController {

//TODO 这里的所有接口要经过权限校验
    @Resource
    private AdminService adminService;

    /**
     * 覆盖Excel数据
     * @param isCover uuid为覆盖，null为不覆盖
     * @param role 1学生，0老师
     * @param request
     * @return
     */
    @Operation(summary = "覆盖Excel数据")
    @PostMapping("/cover")
    public BaseResponse<Object> coverExcel(String isCover, int role, HttpServletRequest request) throws JsonProcessingException {
        adminService.isCover(isCover, role, request);
        return ResultUtils.success(Code.SUCCESS, null, "覆盖成功");
    }

    @Operation(summary = "导出Excel数据")
    @PostMapping("/export")
    public BaseResponse<Object> exportExcel(HttpServletRequest request) {
        boolean result = adminService.exportExcel(request);
        return ResultUtils.success(Code.SUCCESS, result, "导出数据成功");
    }
}
