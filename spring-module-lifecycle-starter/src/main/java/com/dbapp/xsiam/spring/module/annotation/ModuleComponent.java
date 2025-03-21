package com.dbapp.xsiam.spring.module.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 标记一个类为模块组件，该组件会被注册为一个模块并参与模块生命周期管理
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ModuleComponent {

    /**
     * 类名
     */
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";

    /**
     * 模块名称，必须唯一
     */
    String name() default "";

    /**
     * 模块版本
     */
    String version() default "1.0.0";

    /**
     * 初始化顺序，数值越小优先级越高
     */
    int order() default 0;

    /**
     * 依赖的其他模块名称
     */
    String[] dependencies() default {};

    /**
     * 模块的基础包路径，用于包扫描
     * 如果为空，则默认使用该类所在的包路径
     */
    String[] basePackages() default {};

    /**
     * 模块初始化方法名称
     */
    String initMethod() default "";

    /**
     * 模块销毁方法名称
     */
    String destroyMethod() default "";
} 