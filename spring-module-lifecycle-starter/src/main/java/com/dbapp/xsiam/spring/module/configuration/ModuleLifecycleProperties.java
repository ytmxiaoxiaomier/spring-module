package com.dbapp.xsiam.spring.module.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 模块生命周期配置属性
 */
@Data
@ConfigurationProperties(prefix = "module.lifecycle")
public class ModuleLifecycleProperties {

    /**
     * 扫描的基础包路径
     */
    private String[] scanBasePackages = new String[0];

    /**
     * 线程池大小
     */
    private int threadPoolSize = 5;

    /**
     * 初始化超时时间（毫秒）
     */
    private long initTimeout = 60000;

    /**
     * 是否自动初始化模块
     */
    private boolean autoInitialize = true;

    /**
     * 是否启用模块拦截
     */
    private boolean enableInterceptor = true;
} 