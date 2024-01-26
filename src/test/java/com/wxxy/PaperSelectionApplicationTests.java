package com.wxxy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.mapper.UserMapper;
import com.wxxy.service.TeacherService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Data
@SpringBootTest
class PaperSelectionApplicationTests {



    @Autowired
    private TeacherService teacherService;


    @Test
    void contextLoads() {
        String SALT = "paperSelection";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + "123456").getBytes());

        System.out.println(encryptPassword);

    }
}
