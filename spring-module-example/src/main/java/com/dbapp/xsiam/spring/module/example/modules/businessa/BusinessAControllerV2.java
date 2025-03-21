package com.dbapp.xsiam.spring.module.example.modules.businessa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务模块A控制器
 */
@RestController
@RequestMapping("/api/v2/business-a")
public class BusinessAControllerV2 {

    @Autowired
    private BusinessAModule businessAModule;

    @GetMapping("/status")
    public String getStatus() {
        return businessAModule.getStatus();
    }
} 