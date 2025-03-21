package com.dbapp.xsiam.spring.module.event;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.enums.ModuleState;
import org.springframework.context.ApplicationEvent;

/**
 * 模块状态变更事件
 */
public class ModuleStateChangeEvent extends ApplicationEvent {

    private final ModuleState previousState;
    private final ModuleState currentState;

    /**
     * 构造函数
     *
     * @param module        发生状态变更的模块
     * @param previousState 变更前的状态
     * @param currentState  变更后的状态
     */
    public ModuleStateChangeEvent(Module module, ModuleState previousState, ModuleState currentState) {
        super(module);
        this.previousState = previousState;
        this.currentState = currentState;
    }

    /**
     * 获取发生状态变更的模块
     *
     * @return 模块对象
     */
    public Module getModule() {
        return (Module) getSource();
    }

    /**
     * 获取变更前的状态
     *
     * @return 变更前的状态
     */
    public ModuleState getPreviousState() {
        return previousState;
    }

    /**
     * 获取变更后的状态
     *
     * @return 变更后的状态
     */
    public ModuleState getCurrentState() {
        return currentState;
    }

    @Override
    public String toString() {
        return "ModuleStateChangeEvent{" +
                "module=" + getModule().getName() +
                ", previousState=" + previousState +
                ", currentState=" + currentState +
                '}';
    }
} 