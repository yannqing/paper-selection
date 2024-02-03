package com.wxxy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wxxy.domain.User;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@NoArgsConstructor
@Data
@SpringBootTest
class PaperSelectionApplicationTests {



    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserTeamMapper userTeamMapper;


    @Test
    void contextLoads() {
        if (userMapper.exists(new QueryWrapper<User>().eq("userAccount", "yannqing"))) {
            System.out.println("exists");
        }else {
            System.out.println("not exists");
        }


    }
}
