package com.dbapp.xsiam.spring.module.manager;

import com.dbapp.xsiam.spring.module.Module;
import com.dbapp.xsiam.spring.module.enums.ModuleState;
import com.dbapp.xsiam.spring.module.event.ModuleFailedEvent;
import com.dbapp.xsiam.spring.module.event.ModuleReadyEvent;
import com.dbapp.xsiam.spring.module.event.ModuleStateChangeEvent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 模块生命周期管理器，负责管理模块的初始化和销毁流程
 */
@Slf4j
public class ModuleLifecycleManager {

    private final ModuleRegistry registry;
    private final ExecutorService executorService;
    private final ApplicationEventPublisher eventPublisher;
    private final long initTimeout;

    /**
     * 构造函数
     *
     * @param registry       模块注册中心
     * @param threadPoolSize 线程池大小
     * @param initTimeout    初始化超时时间（毫秒）
     * @param eventPublisher 事件发布器
     */
    public ModuleLifecycleManager(ModuleRegistry registry, int threadPoolSize, long initTimeout, ApplicationEventPublisher eventPublisher) {
        Assert.notNull(registry, "ModuleRegistry must not be null");
        Assert.notNull(eventPublisher, "ApplicationEventPublisher must not be null");
        Assert.isTrue(threadPoolSize > 0, "Thread pool size must be positive");
        Assert.isTrue(initTimeout > 0, "Init timeout must be positive");

        this.registry = registry;

        // 初始化线程池 todo 根据后期改为从项目中获取
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread thread = new Thread(r, "module-init-thread");
            thread.setDaemon(true);
            return thread;
        });
        this.initTimeout = initTimeout;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 初始化所有模块
     */
    public void initializeAllModules() {
        List<Module> sortedModules = sortModulesByDependencies();

        if (sortedModules.isEmpty()) {
            log.info("No modules to initialize");
            return;
        }

        log.info("Initializing {} modules in order: {}", sortedModules.size(),
                sortedModules.stream().map(Module::getName).collect(Collectors.joining(", ")));

        Map<String, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();

        for (Module module : sortedModules) {
            CompletableFuture<Void> future = createModuleInitializationFuture(module, futures);
            futures.put(module.getName(), future);
        }

        // 等待所有模块初始化完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[0]));

        try {
            allFutures.get(initTimeout, TimeUnit.MILLISECONDS);
            log.info("All modules initialized successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Module initialization interrupted");
        } catch (ExecutionException e) {
            log.error("Module initialization failed", e.getCause());
        } catch (TimeoutException e) {
            log.error("Module initialization timed out after {} ms", initTimeout);
        }
    }

    /**
     * 初始化单个模块
     *
     * @param moduleName 模块名称
     * @return 模块初始化是否成功
     */
    public boolean initializeModule(String moduleName) {
        Module module = registry.getModule(moduleName);
        if (module == null) {
            log.warn("Module [{}] not found, cannot initialize", moduleName);
            return false;
        }

        if (module.getState() == ModuleState.READY) {
            log.info("Module [{}] is already initialized", moduleName);
            return true;
        }

        try {
            Map<String, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();
            CompletableFuture<Void> future = createModuleInitializationFuture(module, futures);
            future.get(initTimeout, TimeUnit.MILLISECONDS);
            return module.getState() == ModuleState.READY;
        } catch (Exception e) {
            log.error("Failed to initialize module [{}]", moduleName, e);
            return false;
        }
    }

    /**
     * 销毁模块
     *
     * @param moduleName 模块名称
     * @return 是否成功销毁
     */
    public boolean destroyModule(String moduleName) {
        Module module = registry.getModule(moduleName);
        if (module == null) {
            log.warn("Module [{}] not found, cannot destroy", moduleName);
            return false;
        }

        try {
            ModuleState previousState = module.getState();
            module.destroy();
            registry.unregisterModule(moduleName);
            publishStateChangeEvent(module, previousState, ModuleState.UNREGISTERED);
            return true;
        } catch (Exception e) {
            log.error("Failed to destroy module [{}]", moduleName, e);
            return false;
        }
    }

    /**
     * 关闭管理器，释放资源
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    /**
     * 为模块创建初始化Future
     *
     * @param module  要初始化的模块
     * @param futures 已创建的Future集合
     * @return 初始化Future
     */
    private CompletableFuture<Void> createModuleInitializationFuture(Module module, Map<String, CompletableFuture<Void>> futures) {
        Set<String> dependencies = module.getDependencies();

        if (dependencies.isEmpty()) {
            return CompletableFuture.runAsync(() -> initializeModuleInternal(module), executorService);
        }

        // 创建依赖Future数组
        List<CompletableFuture<Void>> dependencyFutures = new ArrayList<>();

        for (String dependency : dependencies) {
            if (futures.containsKey(dependency)) {
                dependencyFutures.add(futures.get(dependency));
            } else {
                Module dependencyModule = registry.getModule(dependency);
                if (dependencyModule != null) {
                    CompletableFuture<Void> depFuture = createModuleInitializationFuture(dependencyModule, futures);
                    futures.put(dependency, depFuture);
                    dependencyFutures.add(depFuture);
                } else {
                    log.warn("Dependency [{}] for module [{}] not found", dependency, module.getName());
                }
            }
        }

        // 当所有依赖完成后，初始化当前模块
        return CompletableFuture.allOf(dependencyFutures.toArray(new CompletableFuture[0]))
                .thenRunAsync(() -> initializeModuleInternal(module), executorService);
    }

    /**
     * 初始化模块内部实现
     *
     * @param module 要初始化的模块
     */
    private void initializeModuleInternal(Module module) {
        String moduleName = module.getName();
        log.info("Starting initialization of module [{}]", moduleName);

        try {
            ModuleState previousState = module.getState();
            module.initialize();
            ModuleState currentState = module.getState();

            // 发布状态变更事件
            publishStateChangeEvent(module, previousState, currentState);

            // 如果模块初始化成功，发布就绪事件
            if (currentState == ModuleState.READY) {
                eventPublisher.publishEvent(new ModuleReadyEvent(module));
                log.info("Module [{}] initialized successfully", moduleName);
            } else if (currentState == ModuleState.FAILED) {
                eventPublisher.publishEvent(new ModuleFailedEvent(module, null));
                log.error("Module [{}] initialization failed", moduleName);
            }
        } catch (Exception e) {
            module.setState(ModuleState.FAILED);
            eventPublisher.publishEvent(new ModuleFailedEvent(module, e));
            log.error("Module [{}] initialization failed with exception", moduleName, e);
            throw e; // 重新抛出异常以通知CompletableFuture
        }
    }

    /**
     * 发布模块状态变更事件
     *
     * @param module        模块
     * @param previousState 先前状态
     * @param currentState  当前状态
     */
    private void publishStateChangeEvent(Module module, ModuleState previousState, ModuleState currentState) {
        if (previousState != currentState) {
            eventPublisher.publishEvent(new ModuleStateChangeEvent(module, previousState, currentState));
        }
    }

    /**
     * 按依赖关系排序模块
     *
     * @return 排序后的模块列表
     */
    private List<Module> sortModulesByDependencies() {
        List<Module> modules = new ArrayList<>(registry.getAllModules());

        // 按order排序，数值小的排在前面
        modules.sort(Comparator.comparingInt(Module::getOrder));

        // 检查循环依赖
        checkForCircularDependencies(modules);

        // 拓扑排序
        return topologicalSort(modules);
    }

    /**
     * 检查模块间是否存在循环依赖
     *
     * @param modules 模块列表
     */
    private void checkForCircularDependencies(List<Module> modules) {
        Map<String, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getName, module -> module));

        for (Module module : modules) {
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();

            if (hasCycleDFS(module.getName(), moduleMap, visited, recursionStack)) {
                throw new IllegalStateException("Circular dependency detected involving module: " + module.getName());
            }
        }
    }

    /**
     * 使用深度优先搜索检测循环依赖
     *
     * @param moduleName     当前模块名
     * @param moduleMap      模块映射
     * @param visited        已访问模块集合
     * @param recursionStack 递归栈
     * @return 是否存在循环
     */
    private boolean hasCycleDFS(String moduleName,
                                Map<String, Module> moduleMap,
                                Set<String> visited,
                                Set<String> recursionStack) {
        if (recursionStack.contains(moduleName)) {
            return true;
        }

        if (visited.contains(moduleName)) {
            return false;
        }

        Module module = moduleMap.get(moduleName);
        if (module == null) {
            return false;
        }

        visited.add(moduleName);
        recursionStack.add(moduleName);

        for (String dep : module.getDependencies()) {
            if (hasCycleDFS(dep, moduleMap, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(moduleName);
        return false;
    }

    /**
     * 对模块进行拓扑排序
     *
     * @param modules 模块列表
     * @return 排序后的模块列表
     */
    private List<Module> topologicalSort(List<Module> modules) {
        Map<String, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getName, module -> module));

        Map<String, Set<String>> graph = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // 构建图和入度表
        for (Module module : modules) {
            String moduleName = module.getName();
            graph.put(moduleName, new HashSet<>());
            inDegree.put(moduleName, 0);
        }

        // 填充图和入度表
        for (Module module : modules) {
            String moduleName = module.getName();
            for (String dep : module.getDependencies()) {
                if (moduleMap.containsKey(dep)) {
                    graph.get(dep).add(moduleName);
                    inDegree.put(moduleName, inDegree.get(moduleName) + 1);
                }
            }
        }

        // 拓扑排序
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> topOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            String moduleName = queue.poll();
            topOrder.add(moduleName);

            for (String dependent : graph.get(moduleName)) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.add(dependent);
                }
            }
        }

        if (topOrder.size() != modules.size()) {
            throw new IllegalStateException("Cyclic dependency detected");
        }

        // 按照拓扑顺序返回模块
        return topOrder.stream()
                .map(moduleMap::get)
                .collect(Collectors.toList());
    }

    @PreDestroy
    public void destroy() {
        for (Module module : registry.getAllModules()) {
            boolean destroyed = destroyModule(module.getName());
            if (destroyed) {
                log.info("Module [{}] destroyed successfully", module.getName());
            } else {
                log.error("Module [{}] destroying failed", module.getName());
            }
        }
        shutdown();
    }
} 