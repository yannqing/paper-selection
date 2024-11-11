package com.wxxy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.util.UUID;

@Tag(name = "文件接口")
@Controller
public class ImageController {


    /**
     * 下载文件
     * @param filename
     * @return
     */
    @Operation(summary = "下载图片文件")
    @GetMapping("/download/image/{filename}")
    public ResponseEntity<FileSystemResource> downloadImage(@PathVariable("filename") String filename) {

        String imagePath = "./images/" + filename; // 图片的本地路径

        return getFileSystemResourceResponseEntity(filename, imagePath);
    }

    /**
     * 下载文件
     * @param filename
     * @return
     */
    @Operation(summary = "下载excel文件")
    @GetMapping("/download/export/{filename}")
    public ResponseEntity<FileSystemResource> downloadExcel(@PathVariable("filename") String filename) {

        String imagePath = "./export/" + filename; // 图片的本地路径

        return getFileSystemResourceResponseEntity(filename, imagePath);
    }

    private ResponseEntity<FileSystemResource> getFileSystemResourceResponseEntity(@PathVariable("filename") String filename, String imagePath) {
        File imageFile = new File(imagePath);

        if (imageFile.exists()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new FileSystemResource(imageFile));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}