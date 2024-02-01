package com.wxxy.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TeacherVo {

    private Long id;

    /**
     * 教师名称
     */
    private String name;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 描述（个人简介）
     */
    private String description;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 最大人数
     */
    private Integer maxNum;

}
