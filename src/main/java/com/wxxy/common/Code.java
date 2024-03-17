package com.wxxy.common;

public class Code {

    public static final Integer SUCCESS = 200;              //一般的成功操作
    public static final Integer FAILURE = 500;              //一般的失败操作

    public static final Integer LOGIN_SUCCESS = 20001;      //登录成功
    public static final Integer LOGIN_FAILURE = 20000;      //登录失败
    public static final Integer LOGOUT_SUCCESS = 20010;     //退出成功

    public static final Integer AUTH_ERROR = 20011; //身份过期

    public static final Integer CHANGE_SIZE_SUCCESS = 30001; //修改数量成功

    public static final Integer CHANGE_SIZE_FAILURE = 30000; //修改数量失败

    public static final Integer DISTRIBUTE_SUCCESS = 40001;     //随机分配，正好分配完

    public static final Integer DISTRIBUTE_STUDENT_REMAINING = 40002;   //随机分配，学生有剩余

    public static final Integer DISTRIBUTE_TEACHER_REMAINING = 40003;   //随机分配，队伍名额有剩余



}
