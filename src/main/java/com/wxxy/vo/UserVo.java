package com.wxxy.vo;

import com.wxxy.domain.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class UserVo {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 姓名
     */
    private String username;

    /**
     * 学院
     */
    private String academy;

    /**
     * 专业
     */
    private String degree;

    /**
     * 学号
     */
    private String userAccount;

    /**
     * 性别：0-女，1-男
     */
    private Integer gender;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    public static UserVo userToVo(User user) {
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        return userVo;
    }

}
