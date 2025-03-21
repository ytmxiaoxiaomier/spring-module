package com.dbapp.xsiam.spring.module.example.modules.businessa;

import com.dbapp.xsiam.spring.module.annotation.ModuleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 业务模块A控制器
 */
@ModuleController(module = "business_a")
@RequestMapping("/api/business-a")
public class BusinessAController {

    @Autowired
    private BusinessAModule businessAModule;

    @GetMapping("/status")
    public String getStatus() {
        return businessAModule.getStatus();
    }
} 