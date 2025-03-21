package com.dbapp.xsiam.spring.module.example.modules.businessb;

import com.dbapp.xsiam.spring.module.annotation.ModuleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 业务模块B控制器
 */
@ModuleController(module = "business_b")
@RequestMapping("/api/business-b")
public class BusinessBController {

    @Autowired
    private BusinessBModule businessBModule;

    @GetMapping("/status")
    public String getStatus() {
        return businessBModule.getStatus();
    }
} 