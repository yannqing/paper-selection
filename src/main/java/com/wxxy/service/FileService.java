package com.wxxy.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    boolean testInputStudentData(MultipartFile multipartFile) throws IOException;

    boolean testInputTeacherData(MultipartFile multipartFile) throws IOException;

}
