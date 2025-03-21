package com.dbapp.xsiam.spring.module.event;

import com.dbapp.xsiam.spring.module.Module;
import org.springframework.context.ApplicationEvent;

/**
 * 模块就绪事件，当模块初始化完成并进入就绪状态时触发
 */
public class ModuleReadyEvent extends ApplicationEvent {

    /**
     * 构造函数
     *
     * @param module 就绪的模块
     */
    public ModuleReadyEvent(Module module) {
        super(module);
    }

    /**
     * 获取就绪的模块
     *
     * @return 模块对象
     */
    public Module getModule() {
        return (Module) getSource();
    }

    @Override
    public String toString() {
        return "ModuleReadyEvent{module=" + getModule().getName() + '}';
    }
} 