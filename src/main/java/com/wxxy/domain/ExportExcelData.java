package com.wxxy.domain;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
public class ExportExcelData {
    @ExcelProperty("教师姓名")
    private String teacherName;
    @ExcelProperty("教师工号")
    private String teacherAccount;
    @ExcelProperty("队伍数量")
    private String teamNumber;
    @ExcelProperty("申请数量")
    private String applyNumber;
    @ExcelProperty("学生姓名")
    private String studentName;
    @ExcelProperty("学生班级")
    private String studentClass;
    @ExcelProperty("学生学号")
    private String studentAccount;
    @ExcelProperty("学生状态")
    private String studentStatus;
    /**
     * 忽略这个字段
     */
//    @ExcelIgnore
//    private String ignore;
}