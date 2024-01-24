package com.wxxy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wxxy.mapper")
public class PaperSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperSelectionApplication.class, args);
    }

}
