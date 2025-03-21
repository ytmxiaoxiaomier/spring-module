package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.annotation.ModuleComponent;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * 模块组件注册器，用于扫描和注册带有@ModuleComponent注解的类
 */
public class ModuleComponentRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,
                                        @NonNull BeanDefinitionRegistry registry) {

        String[] scanBasePackages = System.getProperty("module.lifecycle.scan-base-packages", "").split(",");
        String[] scanBasePackageClasses = System.getProperty("module.lifecycle.scan-base-package-classes", "").split(",");

        // 如果没有指定扫描包，则不进行扫描
        if ((scanBasePackages.length == 1 && scanBasePackages[0].isEmpty()) &&
                (scanBasePackageClasses.length == 1 && scanBasePackageClasses[0].isEmpty())) {
            return;
        }

        // 创建自定义的扫描器
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ModuleComponent.class));

        // 添加扫描包
        for (String basePackage : scanBasePackages) {
            if (StringUtils.hasText(basePackage)) {
                scanner.scan(basePackage);
            }
        }

        for (String basePackage : scanBasePackageClasses) {
            if (StringUtils.hasText(basePackage)) {
                scanner.scan(basePackage);
            }
        }
    }
}
