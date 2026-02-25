package com.example.time;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.time.mapper")
public class TimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeApplication.class, args);
    }
}
