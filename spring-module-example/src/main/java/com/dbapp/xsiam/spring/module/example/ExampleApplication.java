package com.dbapp.xsiam.spring.module.example;

import com.dbapp.xsiam.spring.module.annotation.EnableModuleLifecycle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 示例应用程序入口类
 */
@SpringBootApplication
@EnableModuleLifecycle(
        scanBasePackages = "com.dbapp.xsiam.spring.module.example.modules",
        threadPoolSize = 3,
        initTimeout = 1000
)
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
} 