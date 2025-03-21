package com.dbapp.xsiam.spring.module.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.*;

/**
 * 标记一个控制器属于特定模块，当模块未就绪时，该控制器下的所有请求将被拦截
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface ModuleController {

    /**
     * bean name
     */
    @AliasFor(annotation = Controller.class)
    String value() default "";

    /**
     * 模块名称
     */
    String module() default "";
} 