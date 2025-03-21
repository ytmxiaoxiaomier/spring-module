package com.dbapp.xsiam.spring.module.manager;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.enums.ModuleState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模块注册中心，管理所有模块的注册信息
 */
@Slf4j
public class ModuleRegistry {

    private final Map<String, Module> modules = new ConcurrentHashMap<>();

    /**
     * 注册模块
     *
     * @param module 要注册的模块
     * @return 注册成功返回true，否则返回false
     */
    public boolean registerModule(Module module) {
        Assert.notNull(module, "Module must not be null");
        String moduleName = module.getName();

        if (modules.containsKey(moduleName)) {
            log.warn("Module [{}] already registered, will be replaced", moduleName);
        }

        modules.put(moduleName, module);
        module.setState(ModuleState.REGISTERED);
        log.info("Module [{}] registered successfully", moduleName);
        return true;
    }

    /**
     * 注销模块
     *
     * @param moduleName 要注销的模块名称
     * @return 注销的模块，如果不存在则返回null
     */
    public Module unregisterModule(String moduleName) {
        Assert.hasText(moduleName, "Module name must not be empty");

        Module module = modules.remove(moduleName);
        if (module != null) {
            log.info("Module [{}] unregistered", moduleName);
        } else {
            log.warn("Module [{}] not found, cannot unregister", moduleName);
        }

        return module;
    }

    /**
     * 获取模块
     *
     * @param moduleName 模块名称
     * @return 模块实例，如果不存在则返回null
     */
    public Module getModule(String moduleName) {
        return modules.get(moduleName);
    }

    /**
     * 获取所有已注册的模块
     *
     * @return 所有模块的不可修改集合
     */
    public Collection<Module> getAllModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    /**
     * 获取模块状态
     *
     * @param moduleName 模块名称
     * @return 模块状态，如果模块不存在则返回UNREGISTERED
     */
    public ModuleState getModuleState(String moduleName) {
        Module module = getModule(moduleName);
        return module != null ? module.getState() : ModuleState.UNREGISTERED;
    }

    /**
     * 判断模块是否就绪
     *
     * @param moduleName 模块名称
     * @return true如果模块存在且状态为READY
     */
    public boolean isModuleReady(String moduleName) {
        return getModuleState(moduleName).isReady();
    }

    /**
     * 检查指定包路径是否属于某个模块
     *
     * @param packageName 包路径
     * @return 匹配的模块，如果没有匹配则返回null
     */
    public Module findModuleByPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return null;
        }

        for (Module module : getAllModules()) {
            for (String basePackage : module.getBasePackages()) {
                if (packageName.startsWith(basePackage)) {
                    return module;
                }
            }
        }

        return null;
    }

    /**
     * 获取模块数量
     *
     * @return 注册的模块数量
     */
    public int getModuleCount() {
        return modules.size();
    }

    /**
     * 判断是否存在指定名称的模块
     *
     * @param moduleName 模块名称
     * @return true如果模块存在
     */
    public boolean containsModule(String moduleName) {
        return modules.containsKey(moduleName);
    }
} 