package com.wxxy;
import java.security.SecureRandom;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.domain.UserTeam;
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
        List<String> list = new ArrayList<String>();
        list.add("111");
        list.add("222");
        list.add("333");
        list.add("444");
        list.add("555");
        list.add("666");
        int size = list.size();
        SecureRandom random = new SecureRandom();
        while (size > 0) {
            System.out.print(list);
            int index = random.nextInt(size);
            System.out.println(":"+list.get(index));
            list.remove(index);
            size --;
        }
    }

    @Test
    public void test(){
        int currentNum = teacherMapper.update(new UpdateWrapper<Teacher>().set("currentNum", 0));
        System.out.println(currentNum);
    }
    public static String replaceFilename(String filename, String newName) {
        // 使用正则表达式匹配文件名和扩展名部分
        String regex = "(.*)(\\..*)";
        // 将文件名替换为新名称
        String replacedFilename = filename.replaceAll(regex, newName + "$2");
        return replacedFilename;
    }
}
