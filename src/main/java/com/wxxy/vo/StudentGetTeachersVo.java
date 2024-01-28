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
    private int joinedNum;
    private int remainingNum;
}
