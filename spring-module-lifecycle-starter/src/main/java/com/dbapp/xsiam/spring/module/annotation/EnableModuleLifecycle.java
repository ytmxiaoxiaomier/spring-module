package com.dbapp.xsiam.spring.module.annotation;

import com.dbapp.xsiam.spring.module.configuration.ModuleLifecycleImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用模块生命周期管理功能
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ModuleLifecycleImportSelector.class)
public @interface EnableModuleLifecycle {

    /**
     * 要扫描的基础包路径
     */
    String[] scanBasePackages() default {};

    /**
     * 要扫描的基础包类，会使用这些类所在的包作为基础包路径
     */
    Class<?>[] scanBasePackageClasses() default {};

    /**
     * 模块初始化线程池大小
     */
    int threadPoolSize() default 5;

    /**
     * 模块初始化超时时间（毫秒）
     */
    long initTimeout() default 5000;
} 