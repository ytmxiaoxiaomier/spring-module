package com.dbapp.xsiam.spring.module.event;

import com.dbapp.xsiam.spring.module.Module;
import org.springframework.context.ApplicationEvent;

/**
 * 模块初始化失败事件，当模块初始化失败时触发
 */
public class ModuleFailedEvent extends ApplicationEvent {

    private final Throwable cause;

    /**
     * 构造函数
     *
     * @param module 失败的模块
     * @param cause  失败原因
     */
    public ModuleFailedEvent(Module module, Throwable cause) {
        super(module);
        this.cause = cause;
    }

    /**
     * 获取失败的模块
     *
     * @return 模块对象
     */
    public Module getModule() {
        return (Module) getSource();
    }

    /**
     * 获取失败原因
     *
     * @return 失败原因
     */
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ModuleFailedEvent{" +
                "module=" + getModule().getName() +
                ", cause=" + (cause != null ? cause.getMessage() : "unknown") +
                '}';
    }
} 