package com.dbapp.xsiam.spring.module.example.modules.businessa;

import com.dbapp.xsiam.spring.module.annotation.ModuleComponent;
import com.dbapp.xsiam.spring.module.example.modules.core.CoreModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 业务模块A，依赖于核心模块
 */
@Slf4j
@ModuleComponent(
        name = "business_a",
        version = "1.0.0",
        order = 10,
        dependencies = {"core"},
        initMethod = "init",
        destroyMethod = "destroy"
)
public class BusinessAModule {

    @Autowired
    private CoreModule coreModule;

    /**
     * 模块初始化方法
     */
    public void init() {
        log.info("Business Module A initializing...");
        log.info("Core module status: {}", coreModule.getCoreSetting());

        try {
            // 模拟耗时操作
            Thread.sleep(10000);
            log.info("Business Module A initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Business Module A initialization interrupted", e);
        }
    }

    /**
     * 模块销毁方法
     */
    public void destroy() {
        log.info("Business Module A destroying...");
    }

    /**
     * 获取模块状态
     */
    public String getStatus() {
        return "Business Module A is running";
    }
} 