package com.dbapp.xsiam.spring.module.web;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.annotation.ModuleController;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 模块请求映射处理器映射，扩展Spring MVC的请求映射处理，支持模块控制器
 */
@Slf4j
public class ModuleRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final ModuleRegistry moduleRegistry;
    private final Map<Class<?>, String> moduleMap = new ConcurrentHashMap<>();
    private final Map<String, Set<RequestMappingInfo>> modulePathMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> moduleUrlPatterns = new ConcurrentHashMap<>();

    public ModuleRequestMappingHandlerMapping(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
        setOrder(0); // 设置优先级高于默认的RequestMappingHandlerMapping
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        super.registerHandlerMethod(handler, method, mapping);

        // 记录控制器方法对应的模块
        if (handler instanceof Class) {
            Class<?> handlerClass = (Class<?>) handler;
            String moduleName = resolveModuleName(handlerClass);

            if (StringUtils.hasText(moduleName)) {
                recordModuleMapping(moduleName, mapping);
            }
        } else if (handler.getClass().getName().contains("$$")) {
            // 对于代理类，尝试获取原始类
            Class<?> handlerClass = handler.getClass().getSuperclass();
            String moduleName = resolveModuleName(handlerClass);

            if (StringUtils.hasText(moduleName)) {
                recordModuleMapping(moduleName, mapping);
            }
        }
    }

    /**
     * 记录模块映射关系
     */
    private void recordModuleMapping(String moduleName, RequestMappingInfo mapping) {
        modulePathMap.computeIfAbsent(moduleName, k -> new HashSet<>()).add(mapping);

        // 改用getPathPatternsCondition()获取路径模式
        PathPatternsRequestCondition pathPatterns = mapping.getPathPatternsCondition();
        if (pathPatterns != null) {
            // 提取PathPattern并转换为字符串
            Set<String> patterns = pathPatterns.getPatterns()
                    .stream()
                    .map(PathPattern::getPatternString)
                    .collect(Collectors.toSet());

            if (!CollectionUtils.isEmpty(patterns)) {
                moduleUrlPatterns.computeIfAbsent(moduleName, k -> new HashSet<>()).addAll(patterns);
            }
        }
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

        if (info != null) {
            // 缓存控制器类与模块的映射关系
            if (!moduleMap.containsKey(handlerType)) {
                String moduleName = resolveModuleName(handlerType);
                if (StringUtils.hasText(moduleName)) {
                    moduleMap.put(handlerType, moduleName);

                    // 记录模块与映射的关系
                    recordModuleMapping(moduleName, info);
                }
            }
        }

        return info;
    }

    /**
     * 解析控制器所属的模块名称
     *
     * @param handlerType 控制器类型
     * @return 模块名称
     */
    private String resolveModuleName(Class<?> handlerType) {
        // 优先从注解获取
        ModuleController moduleAnnotation = AnnotationUtils.findAnnotation(handlerType, ModuleController.class);
        if (moduleAnnotation != null) {
            String moduleName = moduleAnnotation.module();
            if (StringUtils.hasText(moduleName)) {
                return moduleName;
            }
        }

        // 尝试从包名推断
        String packageName = handlerType.getPackage().getName();
        Module module = moduleRegistry.findModuleByPackage(packageName);
        if (module != null) {
            return module.getName();
        }

        return null;
    }

    /**
     * 获取控制器所属的模块名称
     *
     * @param handlerType 控制器类型
     * @return 模块名称
     */
    public String getModuleForHandler(Class<?> handlerType) {
        return moduleMap.get(handlerType);
    }

    /**
     * 获取模块的所有请求映射信息
     *
     * @param moduleName 模块名称
     * @return 请求映射信息集合
     */
    public Set<RequestMappingInfo> getModuleMappings(String moduleName) {
        return modulePathMap.getOrDefault(moduleName, Collections.emptySet());
    }

    /**
     * 获取模块的所有URL模式
     *
     * @param moduleName 模块名称
     * @return URL模式集合
     */
    public Set<String> getModuleUrlPatterns(String moduleName) {
        return moduleUrlPatterns.getOrDefault(moduleName, Collections.emptySet());
    }

    /**
     * 获取所有模块的URL模式
     *
     * @return 模块名称到URL模式的映射
     */
    public Map<String, Set<String>> getAllModuleUrlPatterns() {
        return Collections.unmodifiableMap(moduleUrlPatterns);
    }

    /**
     * 获取所有模块映射信息
     *
     * @return 所有模块映射信息
     */
    public Map<String, Set<RequestMappingInfo>> getAllModuleMappings() {
        return Collections.unmodifiableMap(modulePathMap);
    }
} 