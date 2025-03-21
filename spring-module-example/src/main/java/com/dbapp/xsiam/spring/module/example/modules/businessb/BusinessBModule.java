package com.dbapp.xsiam.spring.module.example.modules.businessb;

import com.dbapp.xsiam.spring.module.annotation.ModuleComponent;
import com.dbapp.xsiam.spring.module.example.modules.businessa.BusinessAModule;
import com.dbapp.xsiam.spring.module.example.modules.core.CoreModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 业务模块B，依赖于核心模块和业务模块A
 */
@Slf4j
@ModuleComponent(
        name = "business_b",
        version = "1.0.0",
        order = 20,
        dependencies = {"core", "business_a"},
        initMethod = "init",
        destroyMethod = "destroy"
)
public class BusinessBModule {

    @Autowired
    private CoreModule coreModule;

    @Autowired
    private BusinessAModule businessAModule;

    /**
     * 模块初始化方法
     */
    public void init() {
        log.info("Business Module B initializing...");
        log.info("Core module status: {}", coreModule.getCoreSetting());
        log.info("Business Module A status: {}", businessAModule.getStatus());

        try {
            // 模拟耗时操作
            Thread.sleep(60000);
            log.info("Business Module B initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Business Module B initialization interrupted", e);
        }
    }

    /**
     * 模块销毁方法
     */
    public void destroy() {
        log.info("Business Module B destroying...");
    }

    /**
     * 获取模块状态
     */
    public String getStatus() {
        return "Business Module B is running";
    }
} 