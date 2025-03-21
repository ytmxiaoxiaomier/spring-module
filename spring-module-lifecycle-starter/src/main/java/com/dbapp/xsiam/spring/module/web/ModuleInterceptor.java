package com.dbapp.xsiam.spring.module.web;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.annotation.ModuleController;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 模块拦截器，用于拦截未就绪模块的接口调用
 */
@Slf4j
public class ModuleInterceptor implements HandlerInterceptor {

    private final ModuleRegistry moduleRegistry;
    private ModuleRequestMappingHandlerMapping moduleRequestMappingHandlerMapping;

    public ModuleInterceptor(ModuleRegistry moduleRegistry) {
        this.moduleRegistry = moduleRegistry;
    }

    /**
     * 设置模块请求映射处理器映射，与控制器路径关联
     *
     * @param moduleRequestMappingHandlerMapping 模块请求映射处理器映射
     */
    public void setModuleRequestMappingHandlerMapping(ModuleRequestMappingHandlerMapping moduleRequestMappingHandlerMapping) {
        this.moduleRequestMappingHandlerMapping = moduleRequestMappingHandlerMapping;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Class<?> controllerClass = handlerMethod.getBeanType();
        String moduleName = null;

        // 优先从ModuleRequestMappingHandlerMapping获取模块信息
        if (moduleRequestMappingHandlerMapping != null) {
            moduleName = moduleRequestMappingHandlerMapping.getModuleForHandler(controllerClass);
        }

        if (!StringUtils.hasText(moduleName)) {
            // 查找控制器上的模块注解
            ModuleController moduleAnnotation = AnnotationUtils.findAnnotation(controllerClass, ModuleController.class);

            if (moduleAnnotation != null) {
                // 从注解中获取模块名称
                moduleName = moduleAnnotation.module();
            }

            if (!StringUtils.hasText(moduleName)) {
                // 尝试从包名推断模块
                String packageName = controllerClass.getPackage().getName();
                Module module = moduleRegistry.findModuleByPackage(packageName);

                if (module != null) {
                    moduleName = module.getName();
                }
            }
        }

        // 如果没有找到模块，允许请求通过
        if (!StringUtils.hasText(moduleName)) {
            return true;
        }

        // 检查模块是否就绪
        boolean isModuleReady = moduleRegistry.isModuleReady(moduleName);
        if (!isModuleReady) {
            log.warn("Module [{}] is not ready, blocking request to: {}", moduleName, request.getRequestURI());
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Module is initializing, please try again later.\",\"module\":\"" + moduleName + "\"}");
            return false;
        }

        return true;
    }
} 