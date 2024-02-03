package com.wxxy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wxxy.domain.Teacher;
import com.wxxy.domain.User;
import com.wxxy.mapper.TeacherMapper;
import com.wxxy.mapper.UserMapper;
import com.wxxy.mapper.UserTeamMapper;
import com.wxxy.service.FileService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.wxxy.service.impl.AuthServiceImpl.SALT;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private UserTeamMapper userTeamMapper;


    @Override
    public boolean testInputStudentData(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("传入的文件为空");
        }
        // 创建工作簿对象，加载上传的文件
        Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);
        // 遍历工作表中的行
        Iterator<Row> rowIterator = sheet.iterator();
//            List<List<String>> data = new ArrayList<>();
        List<User> users = new ArrayList<>();
        //遍历每行
        int rowIndex = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
//                List<String> rowData = new ArrayList<>();
            if (rowIndex++ == 0) {
                continue;
            }
            // 遍历行中的单元格
            Iterator<Cell> cellIterator = row.cellIterator();
            int cellIndex = 0 ;
            User user = new User();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType() == CellType.NUMERIC) {
                    // 将NUMERIC类型的单元格转换为STRING类型
                    cell.setCellType(CellType.STRING);
                }
                String cellValue = cell.getStringCellValue();
                switch (cellIndex) {
                    case 0: user.setAcademy(cellValue); break;
                    case 1: user.setUserAccount(cellValue); break;
                    case 2: user.setUsername(cellValue); break;
                }
                user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
//                    rowData.add(cellValue);
                cellIndex++;
            }
            users.add(user);
//                data.add(rowData);
            rowIndex ++;
        }
        workbook.close();
//            System.out.println(data);
        for (User user : users) {
//            System.out.println(user);
            if (!userMapper.exists(new QueryWrapper<User>().eq("userAccount", user.getUserAccount()))) {
                userMapper.insert(user);
            }
        }
        return true;
    }

    @Override
    public boolean testInputTeacherData(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("传入的文件为空");
        }
        // 创建工作簿对象，加载上传的文件
        Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        // 获取第一个工作表
        Sheet sheet = workbook.getSheetAt(0);
        // 遍历工作表中的行
        Iterator<Row> rowIterator = sheet.iterator();
//            List<List<String>> data = new ArrayList<>();
        List<Teacher> teachers = new ArrayList<>();
        //遍历每行
        int rowIndex = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
//                List<String> rowData = new ArrayList<>();

            // 遍历行中的单元格
            Iterator<Cell> cellIterator = row.cellIterator();
            int cellIndex = 0 ;
            Teacher teacher = new Teacher();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType() == CellType.NUMERIC) {
                    // 将NUMERIC类型的单元格转换为STRING类型
                    cell.setCellType(CellType.STRING);
                }
                String cellValue = cell.getStringCellValue();
                switch (cellIndex) {
                    case 1: teacher.setUserAccount(cellValue); break;
                    case 2: teacher.setName(cellValue); break;
                }
                teacher.setUserPassword(DigestUtils.md5DigestAsHex((SALT + "123456").getBytes()));
                teacher.setDescription("工作经历总结：\n" +
                        "非常热爱市场销售工作，有着十分饱满的创业激情。在××××两年从事现磨现煮的咖啡市场销售工作中积累了大量的实践经验和客户资源。与省内主要的二百多家咖啡店铺经销商建立了十分密切的联系，并在行业中拥有广泛的业务关系。在某省的咖啡博览会上为公司首次签定了海外的定单。能团结自己的同事一起取得优异的销售业绩。\n" +
                        "工作经历 ：\n" +
                        "2×××年5月—2×××年： 担任某咖啡茶品配送服务部的市场部业务员。主要负责与经销商签定经销合同、办理产品的包装、运输、保险、货款结算、售后产品跟踪、市场反馈以及开拓新的销售渠道等。负责公司新业务员的培训，在实际工作中具体指导和协调业务员的销售工作，并多次受到公司的表扬。\n" +
                        "1999年12月--2000年5月：在某品牌做市场调查员。主要负责以电话形式向客户提取对产品的意见，并填写相应的表单转报给公司。\n" +
                        "教育经历 ：\n" +
                        "1996年9月—1999年7月某省科技职业学院国际经济与贸易专业大专学历。 在校一直担任学生干部，工作认真负责，学习成绩优秀，多次被学院评为优秀学生干部，优秀团干，个人标兵等。\n" +
                        "所获奖励\n" +
                        "1999/06 某某学院：优秀学生干部称号\n" +
                        "1998/10 某某学院：优秀团干，个人标兵称号\n" +
                        "1997/10 某某学院：优秀团干称号\n" +
                        "培训经历\n" +
                        "2000/07--2000/09 某省科技职业学院 通过外销员考试\n" +
                        "2001/03--2001/06 某省科技职业学院 通过报关员考试\n" +
                        "外语水平\n" +
                        "可与外商进行日常常用语沟通，能阅读业务范围内常用术语。\n" +
                        "电脑操作\n" +
                        "熟练使用常用办公软件编辑业务文档，上网收发电子邮件");
                teacher.setAvatarUrl("http://dummyimage.com/100x100");
                teacher.setPhone("19999999999");
                teacher.setEmail("123456@qq.com");
                cellIndex++;
            }
            teachers.add(teacher);
//                data.add(rowData);
            rowIndex ++;
        }
        workbook.close();
//            System.out.println(data);
        for (Teacher teacher : teachers) {
//            System.out.println(user);
            if (!teacherMapper.exists(new QueryWrapper<Teacher>().eq("userAccount", teacher.getUserAccount()))) {
                teacherMapper.insert(teacher);
            }
        }
        return true;
    }
}
