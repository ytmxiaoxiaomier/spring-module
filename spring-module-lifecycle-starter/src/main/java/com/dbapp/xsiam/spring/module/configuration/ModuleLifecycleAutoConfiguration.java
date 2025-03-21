package com.dbapp.xsiam.spring.module.configuration;

import com.dbapp.xsiam.spring.module.manager.ModuleLifecycleManager;
import com.dbapp.xsiam.spring.module.manager.ModuleRegistry;
import com.dbapp.xsiam.spring.module.web.ModuleEndpoint;
import com.dbapp.xsiam.spring.module.web.ModuleInterceptor;
import com.dbapp.xsiam.spring.module.web.ModuleRequestMappingHandlerMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 模块生命周期自动配置类
 * <p>
 * 注：此类可能会被多次导入（通过@EnableModuleLifecycle和spring.factories），
 * 但Spring会确保同一个Bean只被注册一次，所以不会有问题
 */
@AutoConfiguration
@EnableConfigurationProperties(ModuleLifecycleProperties.class)
@Import(ModuleComponentRegistrar.class)
@Slf4j
public class ModuleLifecycleAutoConfiguration {

    /**
     * 创建模块注册中心
     */
    @Bean
    @ConditionalOnMissingBean
    public ModuleRegistry moduleRegistry() {
        return new ModuleRegistry();
    }

    /**
     * 创建模块生命周期管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public ModuleLifecycleManager moduleLifecycleManager(ModuleRegistry moduleRegistry,
                                                         ModuleLifecycleProperties properties,
                                                         ApplicationEventPublisher eventPublisher) {

        return new ModuleLifecycleManager(
                moduleRegistry,
                properties.getThreadPoolSize(),
                properties.getInitTimeout(),
                eventPublisher);
    }

    /**
     * Web相关配置
     */
    @Configuration
    @ConditionalOnWebApplication
    public static class WebConfiguration {

        /**
         * 创建模块拦截器
         */
        @Bean
        @ConditionalOnMissingBean
        public ModuleInterceptor moduleInterceptor(ModuleRegistry moduleRegistry) {
            return new ModuleInterceptor(moduleRegistry);
        }

        /**
         * 配置Web MVC，添加模块拦截器
         */
        @Bean
        public WebMvcConfigurer moduleWebMvcConfigurer(ModuleInterceptor moduleInterceptor,
                                                       ModuleRequestMappingHandlerMapping moduleRequestMappingHandlerMapping) {
            // 将映射处理器关联到拦截器
            moduleInterceptor.setModuleRequestMappingHandlerMapping(moduleRequestMappingHandlerMapping);

            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(@NonNull InterceptorRegistry registry) {
                    // 获取所有模块的URL模式
                    Set<String> allModulePatterns = getAllModulePatterns();

                    if (!allModulePatterns.isEmpty()) {
                        // 只拦截模块控制器的路径
                        log.info("为模块拦截器配置URL模式: {}", allModulePatterns);
                        registry.addInterceptor(moduleInterceptor)
                                .addPathPatterns(allModulePatterns.toArray(new String[0]))
                                .excludePathPatterns("/health/**", "/actuator/**");
                    } else {
                        // 如果没有获取到模块路径，则使用默认的拦截配置
                        log.info("没有找到模块URL模式，使用默认拦截配置");
                        registry.addInterceptor(moduleInterceptor)
                                .addPathPatterns("/**")
                                .excludePathPatterns("/health/**", "/actuator/**");
                    }
                }

                private Set<String> getAllModulePatterns() {
                    Map<String, Set<String>> moduleUrlPatterns = moduleRequestMappingHandlerMapping.getAllModuleUrlPatterns();
                    Set<String> allModulePatterns = new HashSet<>();

                    if (!CollectionUtils.isEmpty(moduleUrlPatterns)) {
                        // 收集所有模块的URL模式
                        moduleUrlPatterns.forEach((moduleName, patterns) -> {
                            if (!CollectionUtils.isEmpty(patterns)) {
                                allModulePatterns.addAll(patterns);
                                log.debug("添加模块[{}]的URL模式: {}", moduleName, patterns);
                            }
                        });
                    }
                    return allModulePatterns;
                }
            };
        }

        /**
         * 创建模块请求映射处理器映射
         */
        @Bean
        @ConditionalOnMissingBean
        public ModuleRequestMappingHandlerMapping moduleRequestMappingHandlerMapping(ModuleRegistry moduleRegistry) {
            return new ModuleRequestMappingHandlerMapping(moduleRegistry);
        }
    }

    /**
     * Actuator相关配置
     */
    @Configuration
    @ConditionalOnClass(HealthIndicator.class)
    public static class ActuatorConfiguration {

        /**
         * 创建模块健康指示器
         */
        @Bean
        @ConditionalOnMissingBean(name = "moduleHealthIndicator")
        public HealthIndicator moduleHealthIndicator(ModuleRegistry moduleRegistry) {
            return new ModuleHealthIndicator(moduleRegistry);
        }

        /**
         * 创建模块Endpoint
         */
        @Bean
        @ConditionalOnMissingBean
        public ModuleEndpoint moduleEndpoint(ModuleRegistry moduleRegistry, ModuleLifecycleManager moduleLifecycleManager) {
            return new ModuleEndpoint(moduleRegistry, moduleLifecycleManager);
        }
    }

    /**
     * 模块后处理器配置
     */
    @Configuration
    public static class PostProcessorConfiguration {

        /**
         * 创建模块组件后处理器
         */
        @Bean
        @ConditionalOnMissingBean
        public ModuleComponentBeanPostProcessor moduleComponentBeanPostProcessor(
                ModuleRegistry moduleRegistry) {
            return new ModuleComponentBeanPostProcessor(moduleRegistry);
        }

        /**
         * 创建模块初始化后处理器
         */
        @Bean
        @ConditionalOnMissingBean
        public ModuleLifecycleSmartInitializingSingleton moduleLifecycleSmartInitializingSingleton(
                ModuleLifecycleManager moduleLifecycleManager) {
            return new ModuleLifecycleSmartInitializingSingleton(moduleLifecycleManager);
        }
    }
} 