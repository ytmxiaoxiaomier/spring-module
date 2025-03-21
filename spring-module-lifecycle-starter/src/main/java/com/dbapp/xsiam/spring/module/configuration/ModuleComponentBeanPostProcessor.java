package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.AnnotatedModule;
import com.dbapp.xsiam.spring.module.annotation.ModuleComponent;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

/**
 * 模块组件Bean后处理器，负责将带有@ModuleComponent注解的Bean注册为模块
 */
@Slf4j
public class ModuleComponentBeanPostProcessor implements BeanPostProcessor {

    private final ModuleRegistry moduleRegistry;

    public ModuleComponentBeanPostProcessor(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        ModuleComponent annotation = AnnotationUtils.findAnnotation(targetClass, ModuleComponent.class);

        if (annotation != null) {
            processModuleComponent(bean, targetClass, annotation);
        }

        return bean;
    }

    private void processModuleComponent(Object bean, Class<?> targetClass, ModuleComponent annotation) {
        String moduleName = getModuleName(targetClass, annotation);
        String version = annotation.version();
        int order = annotation.order();
        String[] dependencies = annotation.dependencies();
        String[] basePackages = getBasePackages(targetClass, annotation);
        String initMethodName = annotation.initMethod();
        String destroyMethodName = annotation.destroyMethod();

        Method initMethod = findDeclaredMethod(targetClass, initMethodName);
        Method destroyMethod = findDeclaredMethod(targetClass, destroyMethodName);

        AnnotatedModule module = new AnnotatedModule(
                moduleName,
                version,
                order,
                new HashSet<>(Arrays.asList(dependencies)),
                basePackages,
                bean,
                initMethod,
                destroyMethod
        );

        moduleRegistry.registerModule(module);
        log.info("Registered module [{}] from bean of type [{}]", moduleName, targetClass.getName());
    }

    private String getModuleName(Class<?> targetClass, ModuleComponent annotation) {
        String name = annotation.name();
        if (!StringUtils.hasText(name)) {
            name = annotation.value();
        }

        if (!StringUtils.hasText(name)) {
            // 使用简单类名作为模块名称
            name = ClassUtils.getShortName(targetClass);
        }

        return name;
    }

    private String[] getBasePackages(Class<?> targetClass, ModuleComponent annotation) {
        String[] basePackages = annotation.basePackages();

        if (basePackages.length == 0) {
            // 如果未指定基础包路径，使用该类所在的包路径
            String packageName = targetClass.getPackage().getName();
            basePackages = new String[]{packageName};
        }

        return basePackages;
    }

    private Method findDeclaredMethod(Class<?> targetClass, String methodName) {
        if (!StringUtils.hasText(methodName)) {
            return null;
        }

        return ReflectionUtils.findMethod(targetClass, methodName);
    }
} 