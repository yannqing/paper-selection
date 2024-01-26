package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.service.TeacherService;
import com.wxxy.service.UserService;
import com.wxxy.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author 67121
* @description 针对表【user】的数据库操作Service实现
* @createDate 2024-01-22 01:11:58
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {




}




