package com.wxxy;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wxxy.domain.User;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisCache redisCache;


    private String uploadUrl;

    @Test
    void contextLoads() throws JsonProcessingException {
        List<User> users = new ArrayList<User>();
        User u1 = new User();
        u1.setId(0L);
        u1.setUsername("u1");
        u1.setAcademy("u1");
        u1.setDegree("u1");
        u1.setUserAccount("1");
        u1.setUserPassword("1");
        u1.setGender(0);
        u1.setProfile("1");
        u1.setPhone("1");
        u1.setEmail("1");
        u1.setUserStatus(0);
        u1.setCreatTime(new Date());
        u1.setUpdateTime(new Date());
        u1.setIsDelete(0);
        u1.setUserRole(0);
        User u2 = new User();
        u2.setId(0L);
        u2.setUsername("u2");
        u2.setAcademy("2");
        u2.setDegree("2");
        u2.setUserAccount("2");
        u2.setUserPassword("2");
        u2.setGender(0);
        u2.setProfile("2");
        u2.setPhone("2");
        u2.setEmail("2");
        u2.setUserStatus(0);
        u2.setCreatTime(new Date());
        u2.setUpdateTime(new Date());
        u2.setIsDelete(0);
        u2.setUserRole(0);
        User u3 = new User();
        u3.setId(0L);
        u3.setUsername("u3");
        u3.setAcademy("3");
        u3.setDegree("3");
        u3.setUserAccount("3");
        u3.setUserPassword("3");
        u3.setGender(0);
        u3.setProfile("3");
        u3.setPhone("3");
        u3.setEmail("3");
        u3.setUserStatus(0);
        u3.setCreatTime(new Date());
        u3.setUpdateTime(new Date());
        u3.setIsDelete(0);
        u3.setUserRole(0);

        users.add(u1);
        users.add(u2);
        users.add(u3);

//        String userinfo = objectMapper.writeValueAsString(users);
//        redisCache.setCacheObject("stu", userinfo);

        String student = redisCache.getCacheObject("stu");
        List<User> getUsers = objectMapper.readValue(student, new TypeReference<List<User>>(){});
        System.out.println(getUsers);
//        System.out.println(l);
    }

    @Test
    public void test(){
        redisCache.setCacheObject("test1", "value1");
    }
    public static String replaceFilename(String filename, String newName) {
        // 使用正则表达式匹配文件名和扩展名部分
        String regex = "(.*)(\\..*)";
        // 将文件名替换为新名称
        String replacedFilename = filename.replaceAll(regex, newName + "$2");
        return replacedFilename;
    }
}
