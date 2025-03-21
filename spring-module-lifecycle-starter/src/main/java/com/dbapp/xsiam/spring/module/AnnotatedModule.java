package com.dbapp.xsiam.spring.module;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 基于注解的模块实现类，支持通过注解配置初始化和销毁方法
 */
@Slf4j
public class AnnotatedModule extends AbstractModule {

    private final Object targetBean;
    private final Method initMethod;
    private final Method destroyMethod;

    /**
     * 构造函数
     *
     * @param name          模块名称
     * @param version       模块版本
     * @param order         初始化顺序
     * @param dependencies  依赖模块集合
     * @param basePackages  基础包路径
     * @param targetBean    目标Bean对象
     * @param initMethod    初始化方法
     * @param destroyMethod 销毁方法
     */
    public AnnotatedModule(String name,
                           String version,
                           int order,
                           Set<String> dependencies,
                           String[] basePackages,
                           Object targetBean,
                           Method initMethod,
                           Method destroyMethod) {
        super(name, version, order, dependencies, basePackages);
        this.targetBean = targetBean;
        this.initMethod = initMethod;
        this.destroyMethod = destroyMethod;
    }

    @Override
    protected void doInitialize() {
        if (initMethod != null) {
            ReflectionUtils.makeAccessible(initMethod);
            try {
                initMethod.invoke(targetBean);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize module: " + name, e);
            }
        }
    }

    @Override
    protected void doDestroy() {
        if (destroyMethod != null) {
            ReflectionUtils.makeAccessible(destroyMethod);
            try {
                destroyMethod.invoke(targetBean);
            } catch (Exception e) {
                log.error("Failed to destroy module: {}", name, e);
            }
        }
    }

    /**
     * 获取目标Bean对象
     *
     * @return 目标Bean对象
     */
    public Object getTargetBean() {
        return targetBean;
    }
} 