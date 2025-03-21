package com.dbapp.xsiam.spring.module;

import com.dbapp.xsiam.spring.module.enums.ModuleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 模块抽象实现类，提供模块接口的基本实现
 */
@Slf4j
public abstract class AbstractModule implements Module {

    protected final String name;
    protected final String version;
    protected final int order;
    protected final Set<String> dependencies;
    protected final String[] basePackages;
    protected ModuleState state = ModuleState.UNREGISTERED;
    protected double progress = 0.0;

    /**
     * 构造函数
     *
     * @param name         模块名称
     * @param version      模块版本
     * @param order        初始化顺序
     * @param dependencies 依赖模块集合
     * @param basePackages 基础包路径
     */
    protected AbstractModule(String name, String version, int order, Set<String> dependencies, String[] basePackages) {
        Assert.hasText(name, "模块名称不能为空");
        this.name = name;
        this.version = version != null ? version : "1.0.0";
        this.order = order;
        this.dependencies = dependencies != null ? new HashSet<>(dependencies) : new HashSet<>();
        this.basePackages = basePackages != null ? basePackages : new String[0];
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    @Override
    public String[] getBasePackages() {
        return basePackages;
    }

    @Override
    public void initialize() {
        if (getState() != ModuleState.REGISTERED) {
            log.warn("Module [{}] is not in REGISTERED state, current state: {}", name, getState());
            return;
        }

        try {
            setState(ModuleState.INITIALIZING);
            log.info("Module [{}] is initializing...", name);
            doInitialize();
            setState(ModuleState.READY);
            log.info("Module [{}] initialization completed", name);
        } catch (Exception e) {
            log.error("Module [{}] initialization failed", name, e);
            setState(ModuleState.FAILED);
        }
    }

    /**
     * 执行具体的初始化逻辑，由子类实现
     */
    protected abstract void doInitialize();

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public ModuleState getState() {
        return state;
    }

    @Override
    public void setState(ModuleState state) {
        this.state = state;
    }

    @Override
    public void destroy() {
        try {
            log.info("Destroying module [{}]...", name);
            doDestroy();
            log.info("Module [{}] destroyed", name);
        } catch (Exception e) {
            log.error("Failed to destroy module [{}]", name, e);
        }
    }

    /**
     * 执行具体的销毁逻辑，由子类实现
     */
    protected abstract void doDestroy();

    /**
     * 更新模块初始化进度
     *
     * @param progress 进度值，范围0.0-1.0
     */
    protected void updateProgress(double progress) {
        if (progress < 0.0) {
            this.progress = 0.0;
        } else if (progress > 1.0) {
            this.progress = 1.0;
        } else {
            this.progress = progress;
        }
    }

    @Override
    public String toString() {
        return "Module{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", state=" + state +
                ", progress=" + progress +
                '}';
    }
} 