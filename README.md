# Spring Module - 模块生命周期管理框架

Spring Module是一个基于Spring Boot的模块生命周期管理框架，用于解决在Spring Boot应用中模块初始化与接口可用性不同步的问题。

## 功能特性

* 模块注册与识别机制
* 模块初始化状态管理
* 模块依赖关系管理
* 模块接口拦截控制
* 模块健康检查与状态监控
* 模块生命周期事件通知

## 项目结构

```
spring-module/
├── spring-module-framework/  - 框架核心模块
└── spring-module-example/    - 使用示例模块
```

## 快速开始

### 添加依赖

```xml

<dependency>
    <groupId>com.dbapp.xsiam</groupId>
    <artifactId>spring-module-framework</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 启用模块生命周期管理

```java

@SpringBootApplication
@EnableModuleLifecycle(
        scanBasePackages = "com.example.modules",
        threadPoolSize = 5,
        initTimeout = 30000
)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 创建模块

```java

@ModuleComponent(
        name = "core",
        version = "1.0.0",
        order = 0,
        initMethod = "init",
        destroyMethod = "destroy"
)
public class CoreModule {

    public void init() {
        // 模块初始化逻辑
    }

    public void destroy() {
        // 模块销毁逻辑
    }
}
```

### 创建模块控制器

```java

@ModuleController(module = "core")
@RequestMapping("/api/core")
public class CoreController {

    @Autowired
    private CoreModule coreModule;

    @GetMapping("/status")
    public String getStatus() {
        return "Core module is running";
    }
}
```

## 核心注解

- `@EnableModuleLifecycle`: 启用模块生命周期管理
- `@ModuleComponent`: 标记一个类为模块组件
- `@ModuleController`: 标记一个控制器属于特定模块

## 模块状态

- `UNREGISTERED`: 未注册状态
- `REGISTERED`: 已注册状态
- `INITIALIZING`: 初始化中状态
- `READY`: 就绪状态
- `FAILED`: 失败状态

## 健康检查

框架提供了Spring Boot Actuator集成，可通过以下端点查看模块状态：

- `/actuator/health`: 查看整体健康状态
- `/actuator/modules`: 查看所有模块详细信息

## 示例运行

1. 克隆项目: `git clone https://github.com/your-username/spring-module.git`
2. 编译项目: `mvn clean install`
3. 运行示例: `cd spring-module-example && mvn spring-boot:run`
4. 访问接口:
    - `http://localhost:8080/api/core/status`
    - `http://localhost:8080/api/business-a/status`
    - `http://localhost:8080/api/business-b/status`
    - `http://localhost:8080/actuator/modules`
    - `http://localhost:8080/actuator/health` 