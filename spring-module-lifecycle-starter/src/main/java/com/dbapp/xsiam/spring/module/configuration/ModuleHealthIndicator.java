package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.enums.ModuleState;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.HashMap;
import java.util.Map;

/**
 * 模块健康指示器，提供模块健康状态信息
 */
public class ModuleHealthIndicator implements HealthIndicator {

    private final ModuleRegistry moduleRegistry;

    public ModuleHealthIndicator(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    public Health health() {
        int readyCount = 0;
        int failedCount = 0;
        int initializingCount = 0;
        int totalCount = moduleRegistry.getModuleCount();

        Map<String, Object> details = new HashMap<>();

        for (Module module : moduleRegistry.getAllModules()) {
            String moduleName = module.getName();
            ModuleState state = module.getState();
            double progress = module.getProgress();

            Map<String, Object> moduleInfo = new HashMap<>();
            moduleInfo.put("state", state.name());
            moduleInfo.put("progress", progress);
            moduleInfo.put("version", module.getVersion());

            details.put(moduleName, moduleInfo);

            if (state == ModuleState.READY) {
                readyCount++;
            } else if (state == ModuleState.FAILED) {
                failedCount++;
            } else if (state == ModuleState.INITIALIZING) {
                initializingCount++;
            }
        }

        details.put("summary", Map.of(
                "total", totalCount,
                "ready", readyCount,
                "failed", failedCount,
                "initializing", initializingCount
        ));

        // 如果所有模块都就绪，则状态为UP
        // 如果有失败的模块，则状态为DOWN
        // 如果有正在初始化的模块，则状态为OUT_OF_SERVICE
        if (failedCount > 0) {
            return Health.down()
                    .withDetails(details)
                    .build();
        } else if (initializingCount > 0) {
            return Health.outOfService()
                    .withDetails(details)
                    .build();
        } else if (readyCount == totalCount && totalCount > 0) {
            return Health.up()
                    .withDetails(details)
                    .build();
        } else {
            return Health.unknown()
                    .withDetails(details)
                    .build();
        }
    }
} 