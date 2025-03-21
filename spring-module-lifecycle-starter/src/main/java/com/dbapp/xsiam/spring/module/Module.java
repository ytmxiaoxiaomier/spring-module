package com.dbapp.xsiam.spring.module;

import com.dbapp.xsiam.spring.module.enums.ModuleState;

import java.util.Set;

/**
 * 模块接口，定义了模块的基本方法和属性
 */
public interface Module {

    /**
     * 获取模块名称
     *
     * @return 模块名称
     */
    String getName();

    /**
     * 获取模块版本
     *
     * @return 模块版本
     */
    String getVersion();

    /**
     * 获取模块初始化顺序，数值越小优先级越高
     *
     * @return 初始化顺序
     */
    int getOrder();

    /**
     * 获取模块依赖的其他模块名称集合
     *
     * @return 依赖模块名称集合
     */
    Set<String> getDependencies();

    /**
     * 获取模块所属的基础包路径
     *
     * @return 基础包路径数组
     */
    String[] getBasePackages();

    /**
     * 初始化模块
     */
    void initialize();

    /**
     * 获取模块初始化进度
     *
     * @return 初始化进度，范围0.0-1.0
     */
    double getProgress();

    /**
     * 获取模块当前状态
     *
     * @return 模块状态
     */
    ModuleState getState();

    /**
     * 设置模块状态
     *
     * @param state 新的模块状态
     */
    void setState(ModuleState state);

    /**
     * 销毁模块
     */
    void destroy();
} 