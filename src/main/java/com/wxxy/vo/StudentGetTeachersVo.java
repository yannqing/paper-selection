package com.wxxy.vo;

import lombok.Data;

@Data
public class StudentGetTeachersVo {
    private Long teacherId;
    private String teacherName;
    private String avatarUrl;
    private String teacherDescription;
    private String phone;
    private String email;
    private int maxNum;
    private int status; //0-未申请，未加入，1-已申请，2-已加入
    private Integer currentNum;
    private Integer maxApply;
    private Integer applyNum;
}
