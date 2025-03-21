package com.dbapp.xsiam.spring.module.enums;

/**
 * 模块状态枚举，表示模块在生命周期中的各个状态
 */
public enum ModuleState {
    /**
     * 未注册状态，模块尚未被注册到系统中
     */
    UNREGISTERED,

    /**
     * 已注册状态，模块已被注册但尚未开始初始化
     */
    REGISTERED,

    /**
     * 初始化中状态，模块正在执行初始化流程
     */
    INITIALIZING,

    /**
     * 就绪状态，模块初始化完成并可以提供服务
     */
    READY,

    /**
     * 失败状态，模块初始化过程中发生错误
     */
    FAILED;

    /**
     * 判断当前状态是否为终态（就绪或失败）
     *
     * @return true如果状态为READY或FAILED
     */
    public boolean isTerminal() {
        return this == READY || this == FAILED;
    }

    /**
     * 判断当前状态是否为就绪状态
     *
     * @return true如果状态为READY
     */
    public boolean isReady() {
        return this == READY;
    }
} 