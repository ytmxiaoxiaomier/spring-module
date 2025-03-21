package com.dbapp.xsiam.spring.module.web;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.enums.ModuleState;
import com.dbapp.xsiam.spring.module.manager.ModuleLifecycleManager;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模块端点，提供模块信息和操作API
 */
@Endpoint(id = "modules")
public class ModuleEndpoint {

    private final ModuleRegistry registry;
    private final ModuleLifecycleManager lifecycleManager;

    public ModuleEndpoint(ModuleRegistry registry, ModuleLifecycleManager lifecycleManager) {
        this.registry = registry;
        this.lifecycleManager = lifecycleManager;
    }

    /**
     * 获取所有模块的信息
     *
     * @return 模块信息映射
     */
    @ReadOperation
    public Map<String, Object> modules() {
        Map<String, Object> result = new HashMap<>();

        result.put("modules", registry.getAllModules().stream()
                .map(this::moduleToMap)
                .collect(Collectors.toList()));

        return result;
    }

    /**
     * 获取指定模块的信息
     *
     * @param moduleName 模块名称
     * @return 模块信息
     */
    @ReadOperation
    public Map<String, Object> module(@Selector String moduleName) {
        Module module = registry.getModule(moduleName);
        if (module == null) {
            return Map.of("error", "Module not found: " + moduleName);
        }

        return moduleToMap(module);
    }

    /**
     * 初始化指定模块
     *
     * @param moduleName 模块名称
     * @return 操作结果
     */
    @WriteOperation
    public Map<String, Object> initialize(@Selector String moduleName) {
        boolean success = lifecycleManager.initializeModule(moduleName);

        if (success) {
            return Map.of(
                    "success", true,
                    "message", "Module [" + moduleName + "] initialized successfully"
            );
        } else {
            return Map.of(
                    "success", false,
                    "message", "Failed to initialize module [" + moduleName + "]"
            );
        }
    }

    /**
     * 将模块对象转换为Map
     *
     * @param module 模块对象
     * @return 包含模块信息的Map
     */
    private Map<String, Object> moduleToMap(Module module) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", module.getName());
        map.put("version", module.getVersion());
        map.put("state", module.getState().name());
        map.put("progress", module.getProgress());
        map.put("order", module.getOrder());
        map.put("dependencies", module.getDependencies());
        map.put("basePackages", module.getBasePackages());
        map.put("isReady", module.getState() == ModuleState.READY);

        return map;
    }
} 