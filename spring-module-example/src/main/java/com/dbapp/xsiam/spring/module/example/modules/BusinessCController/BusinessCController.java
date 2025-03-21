package com.dbapp.xsiam.spring.module.example.modules.BusinessCController;

import com.dbapp.xsiam.spring.module.example.modules.businessa.BusinessAModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 业务模块A控制器
 */
@RestController
@RequestMapping("/api/business-c")
public class BusinessCController {

    @Autowired
    private BusinessAModule businessAModule;

    @GetMapping("/status")
    public String getStatus() {
        return businessAModule.getStatus();
    }
} 