package com.wxxy;
import java.security.SecureRandom;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void test() throws JsonProcessingException {

    }
    public static String replaceFilename(String filename, String newName) {
        // 使用正则表达式匹配文件名和扩展名部分
        String regex = "(.*)(\\..*)";
        // 将文件名替换为新名称
        String replacedFilename = filename.replaceAll(regex, newName + "$2");
        return replacedFilename;
    }
}
