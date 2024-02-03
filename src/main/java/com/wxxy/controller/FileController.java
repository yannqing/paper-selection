package com.wxxy.controller;

import com.wxxy.common.Code;
import com.wxxy.service.FileService;
import com.wxxy.utils.ResultUtils;
import com.wxxy.vo.BaseResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/files")
public class FileController {

    @jakarta.annotation.Resource
    FileService fileService;

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) throws IOException {
        String resourcePath = "static/"+filename; // resources目录下的文件路径

        ClassPathResource classPathResource = new ClassPathResource(resourcePath);
        InputStream inputStream = classPathResource.getInputStream();
        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("attachment", classPathResource.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);


//        Path filePath = Paths.get("src/main/resources/", filename);
//        Resource resource = new ClassPathResource(filePath.toString());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resource.getFilename())
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
    }

    @PostMapping("/testInputStudentData")
    public BaseResponse<Object> testInputStudentData(@RequestParam("file") MultipartFile inputFile) throws IOException {
        boolean result = fileService.testInputStudentData(inputFile);
        if (result) {
            ResultUtils.success(Code.SUCCESS, null, "新增Excel数据成功");
        }
        else ResultUtils.failure(Code.FAILURE, null, "新增Excel数据失败");
        return null;
    }

    @PostMapping("/testInputTeacherData")
    public BaseResponse<Object> testInputTeacherData(@RequestParam("file") MultipartFile inputFile) throws IOException {
        boolean result = fileService.testInputTeacherData(inputFile);
        if (result) {
            ResultUtils.success(Code.SUCCESS, null, "新增Excel数据成功");
        }
        else ResultUtils.failure(Code.FAILURE, null, "新增Excel数据失败");
        return null;
    }
}
