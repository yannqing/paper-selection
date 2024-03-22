package com.wxxy.service;

import com.wxxy.vo.GetAllByPageVo;
import com.wxxy.vo.StudentGetTeachersVo;
import jakarta.servlet.http.HttpServletRequest;

public interface SecondService {


    Object login(String username, String password, HttpServletRequest request);


}
