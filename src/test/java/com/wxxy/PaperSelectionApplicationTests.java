package com.wxxy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.TeacherService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Data
@SpringBootTest
class PaperSelectionApplicationTests {



    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;


    @Test
    void contextLoads() {
//        Page<Teacher> page = new Page<>();
//        page.setSize(3);
//        page.setCurrent(2);
//        List<Teacher> teachers = teacherMapper.testPage(page);
//        System.out.println(teachers.size());
//        for (Teacher teacher : teachers) {
//            System.out.println(teacher);
//        }


        Page<Teacher> page = new Page<>();
        page.setCurrent(2);
        page.setSize(4);
        Page<Teacher> teacher = teacherMapper.selectPage(page, null);
//        List<Teacher> teachers = teacherMapper.testPage(page);
        List<Teacher> teachers = teacher.getRecords();
        System.out.println(teachers);
    }
}
