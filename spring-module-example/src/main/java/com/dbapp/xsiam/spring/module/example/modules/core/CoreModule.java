package com.dbapp.xsiam.spring.module.example.modules.core;

import com.dbapp.xsiam.spring.module.annotation.ModuleComponent;
import lombok.extern.slf4j.Slf4j;

/**
 * 核心模块，其他模块依赖于此模块
 */
@Slf4j
@ModuleComponent(
        name = "core",
        version = "1.0.0",
        order = 0,
        initMethod = "init",
        destroyMethod = "destroy"
)
public class CoreModule {

    /**
     * 模块初始化方法
     */
    public void init() {
        log.info("Core module initializing...");

        try {
            // 模拟耗时操作
            Thread.sleep(2000);
            log.info("Core module initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Core module initialization interrupted", e);
        }
    }

    /**
     * 模块销毁方法
     */
    public void destroy() {
        log.info("Core module destroying...");
    }

    /**
     * 提供给其他模块调用的方法
     */
    public String getCoreSetting() {
        return "Core module is running";
    }
} 