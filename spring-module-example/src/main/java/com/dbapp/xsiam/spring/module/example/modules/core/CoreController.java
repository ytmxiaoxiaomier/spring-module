package com.dbapp.xsiam.spring.module.example.modules.core;

import com.dbapp.xsiam.spring.module.annotation.ModuleController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 核心模块控制器
 */
@ModuleController(module = "core")
@RequestMapping("/api/core")
public class CoreController {

    @Autowired
    private CoreModule coreModule;

    @GetMapping("/status")
    public String getStatus() {
        return coreModule.getCoreSetting();
    }
} 