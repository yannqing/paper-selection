//package com.wxxy;
//import java.security.SecureRandom;
//import java.util.*;
//
//import com.alibaba.excel.EasyExcel;
//import com.alibaba.excel.util.ListUtils;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wxxy.domain.ExportExcelData;
//import com.wxxy.mapper.TeacherMapper;
//import com.wxxy.mapper.UserMapper;
//import com.wxxy.mapper.UserTeamMapper;
//import com.wxxy.service.AdminService;
//import jakarta.annotation.Resource;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@NoArgsConstructor
//@Data
//@SpringBootTest
//class PaperSelectionApplicationTests {
//
//
//
//    @Resource
//    private TeacherMapper teacherMapper;
//
//    @Resource
//    private UserMapper userMapper;
//
//    @Resource
//    private UserTeamMapper userTeamMapper;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @Resource
//    private AdminService adminService;
//
//
//
//    private String uploadUrl;
//
//    @Test
//    void contextLoads() throws JsonProcessingException {
//        List<String> list = new ArrayList<String>();
//        list.add("111");
//        list.add("222");
//        list.add("333");
//        list.add("444");
//        list.add("555");
//        list.add("666");
//        int size = list.size();
//        SecureRandom random = new SecureRandom();
//        while (size > 0) {
//            System.out.print(list);
//            int index = random.nextInt(size);
//            System.out.println(":"+list.get(index));
//            list.remove(index);
//            size --;
//        }
//    }
//
//    @Test
//    public void test() throws JsonProcessingException {
//
//    }
//
//    public static String replaceFilename(String filename, String newName) {
//        // 使用正则表达式匹配文件名和扩展名部分
//        String regex = "(.*)(\\..*)";
//        // 将文件名替换为新名称
//        String replacedFilename = filename.replaceAll(regex, newName + "$2");
//        return replacedFilename;
//    }
//
//    /**
//     * 管理员删除所有学生
//     */
//    @Test
//    public void testAdminDeleteAllUsers() {
//        boolean result = adminService.deleteAllStudents(null);
//        System.out.println("result:" + result);
//    }
//
//    /**
//     * 测试导出 excel
//     * @throws JsonProcessingException
//     */
//    @Test
//    public void testExportExcel() throws JsonProcessingException {
//        adminService.exportExcel(null);
//
//    }
//
//
//    private List<ExportExcelData> data() {
//        List<ExportExcelData> list = ListUtils.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            ExportExcelData data = new ExportExcelData();
//            data.setApplyNumber("申请数量" + i);
//            data.setStudentClass("学生班级" + i);
//            data.setStudentName("学生姓名" + i);
//            data.setStudentAccount("学生学号" + i);
//            data.setStudentStatus("学生状态" + i);
//            data.setTeacherAccount("教师工号" + i);
//            data.setTeacherName("教师姓名" + i);
//            data.setTeamNumber("队伍数量" + i);
//            list.add(data);
//        }
//        return list;
//    }
//}
