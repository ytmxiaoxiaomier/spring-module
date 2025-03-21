package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.manager.ModuleLifecycleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

/**
 * 模块生命周期初始化单例，负责在所有单例Bean初始化完成后启动模块初始化流程
 */
@Slf4j
public class ModuleLifecycleSmartInitializingSingleton implements SmartInitializingSingleton {

    private final ModuleLifecycleManager lifecycleManager;

    public ModuleLifecycleSmartInitializingSingleton(ModuleLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    @Override
    public void afterSingletonsInstantiated() {
        log.info("All singleton beans instantiated, starting module initialization...");
        lifecycleManager.initializeAllModules();
    }
} 