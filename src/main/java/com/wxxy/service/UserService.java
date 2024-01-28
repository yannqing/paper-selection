package com.wxxy.service;

import com.wxxy.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wxxy.vo.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service
* @createDate 2024-01-22 01:11:58
*/
public interface UserService extends IService<User> {

    List<User> getSelectedStudent(HttpServletRequest request);


    boolean agreeJoin(Long userId, HttpServletRequest request);

    List<User> joinedStudent(HttpServletRequest request);

    boolean removeFromTeam(Long userId, HttpServletRequest request);

    boolean changeMaxSize(int maxSize, HttpServletRequest request);

}
