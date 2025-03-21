package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.annotation.EnableModuleLifecycle;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * 模块生命周期导入选择器，用于启用模块生命周期管理功能
 */
public class ModuleLifecycleImportSelector implements DeferredImportSelector {

    @NonNull
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableModuleLifecycle.class.getName(), false));

        if (attributes != null) {
            // 获取注解参数
            String[] scanBasePackages = attributes.getStringArray("scanBasePackages");
            Class<?>[] scanBasePackageClasses = attributes.getClassArray("scanBasePackageClasses");
            int threadPoolSize = attributes.getNumber("threadPoolSize");
            long initTimeout = attributes.getNumber("initTimeout");

            // 设置系统属性，用于ModuleLifecycleAutoConfiguration获取参数
            if (scanBasePackages.length > 0) {
                System.setProperty("module.lifecycle.scan-base-packages", String.join(",", scanBasePackages));
            }

            if (scanBasePackageClasses.length > 0) {
                String[] packageNames = new String[scanBasePackageClasses.length];
                for (int i = 0; i < scanBasePackageClasses.length; i++) {
                    packageNames[i] = scanBasePackageClasses[i].getPackage().getName();
                }
                System.setProperty("module.lifecycle.scan-base-package-classes", String.join(",", packageNames));
            }

            System.setProperty("module.lifecycle.thread-pool-size", String.valueOf(threadPoolSize));
            System.setProperty("module.lifecycle.init-timeout", String.valueOf(initTimeout));
        }

        return new String[]{ModuleLifecycleAutoConfiguration.class.getName()};
    }
} 