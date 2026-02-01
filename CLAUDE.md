# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

Redisfx 是一个使用 JavaFX 22 构建的 Redis GUI 工具，目标平台为 JDK 21。它提供了管理 Redis 连接、查看服务器信息和操作键值的图形界面。

**技术栈：**
- Java 21 with Java Modules (module-info.java)
- JavaFX 22 用于 UI
- AtlantaFX 2.0.1 用于主题 (PrimerLight/PrimerDark themes)
- Jedis 5.0.0 用于 Redis 客户端
- Guava EventBus 用于内部事件处理
- SQLite 用于本地数据持久化
- Maven 用于构建管理

## 常用命令

### 运行应用程序
```bash
# 开发模式运行
mvn clean javafx:run

# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包应用程序（创建平台特定的安装包）
mvn clean install

# 为特定平台打包
mvn clean install -P mac    # macOS
mvn clean install -P unix   # Linux
mvn clean install -P win    # Windows
```

### 开发
```bash
# 清理构建产物
mvn clean

# 复制依赖
mvn dependency:copy-dependencies

# 仅创建运行时镜像
mvn package
```

## 架构

### 应用程序结构

**入口点流程：**
1. `Launcher.main()` → `RedisfxApplication.main()` → `RedisfxApplication.start()`
2. 主 FXML 加载：`views/main-view.fxml` → `MainController`
3. 应用程序使用模块化架构（定义在 `module-info.java` 中）

**核心包：**
- `com.xiebiao.tools.redisfx` - 主应用程序类、启动器
- `com.xiebiao.tools.redisfx.controller` - FXML 控制器（MainController, ConnectionInfoController）
- `com.xiebiao.tools.redisfx.view` - 自定义 JavaFX 视图（ConnectionTitledPane, KeyTabView, ServerInfoTabView, ToastView, MemoryMonitorView, StatisticsView）
- `com.xiebiao.tools.redisfx.model` - 数据模型（RedisConnectionInfo, RedisInfo, KeyPage, DetailInfo）
- `com.xiebiao.tools.redisfx.service.eventbus` - 使用 Guava 的事件总线实现
- `com.xiebiao.tools.redisfx.utils` - 工具类（Constants, Utils, Icons, RedisfxStyles, RedisCommand, RedisKeyTypes）
- `com.xiebiao.tools.redisfx.core` - 核心接口（LifeCycle）

### 事件总线架构

应用程序通过 `RedisfxEventBusService` 使用集中式事件总线模式（封装 Guava EventBus）：
- 控制器/视图通过 `RedisfxEventBusService.post(RedisfxEventMessasge)` 发布事件
- 组件通过 `RedisfxEventBusService.register(this)` / `unregister(this)` 注册/注销
- 在处理方法上使用 `@Subscribe` 注解
- 事件类型定义在 `RedisfxEventType` 枚举中
- 示例：`NEW_CONNECTION_CREATED` 事件触发 MainController 中的 UI 更新

### 线程模型

- 主 JavaFX 应用程序线程用于 UI 操作
- `RedisfxApplication.mainThreadPool` - ThreadPoolExecutor（5-10 个线程）用于后台任务
- 使用 `Platform.runLater()` 从后台线程更新 UI
- 每个连接都有用于自动刷新功能的定时执行器

### 连接管理

**ConnectionTitledPane** 是管理 Redis 连接的核心组件：
- 为每个连接创建和管理 JedisPool
- 将数据库显示为选项卡（ServerInfoTab, KeyInfoTab）
- 通过 ChoiceBox 处理数据库选择
- 实现 LifeCycle 接口进行清理
- 通过 KeyTabView 执行键值操作
- 通过 ServerInfoTabView 进行服务器监控

### 视图模式

自定义视图扩展 JavaFX 组件并实现创建方法：
- 视图以编程方式组合（非基于 FXML）
- 大多数视图都有一个返回组件的 `create()` 方法
- 视图订阅事件总线以进行跨组件通信
- 视图在管理资源时实现 LifeCycle 接口

### FXML 视图

主布局使用有限的 FXML：
- `main-view.fxml` → MainController（带分隔面板的主窗口）
- `connectioninfo-view.fxml` → ConnectionInfoController（连接对话框）
- `connection-view.fxml`（如果存在）

## 关键实现模式

### 添加新的 Redis 操作
1. 如果需要，向 `RedisCommand` 添加命令常量
2. 在 KeyTabView 中实现操作或创建新视图
3. 使用来自 ConnectionTitledPane 的 JedisPool
4. 将 Jedis 操作包装在 try-with-resources 中
5. 如果其他组件需要响应，通过 RedisfxEventBusService 发布事件
6. 如果从后台线程调用，在 Platform.runLater() 上更新 UI

### 创建新视图
1. 在 `com.xiebiao.tools.redisfx.view` 包中创建类
2. 如果管理资源，实现 LifeCycle
3. 添加返回 JavaFX 节点的 `create()` 方法
4. 如果需要，注册到事件总线：`RedisfxEventBusService.register(this)`
5. 在 destroy() 中清理：从事件总线注销、关闭资源
6. 如果需要，在 module-info.java 中导出包

### 主题支持
- 应用程序使用 AtlantaFX 主题（PrimerLight, PrimerDark, Primitive）
- 主题切换在 MainController 中通过菜单项处理
- 自定义样式定义在 RedisfxStyles 中
- 使用 AtlantaFX 样式类（Styles.BUTTON_*）以保持主题一致

### 资源管理
- 实现 LifeCycle 的组件必须在 destroy() 中清理
- MainController.destroy() 级联到所有 ConnectionTitledPane 实例
- JedisPool 必须在删除连接时关闭
- EventBus 监听器必须取消注册

## 构建配置

Maven 构建使用 jlink 和 jpackage 创建自包含应用程序：
- **jlink** 创建仅包含所需模块的自定义 JRE
- **jpackage** 将应用程序与 JRE 打包成平台特定的包
- 依赖项复制到 `target/dependencies`
- JavaFX 模块单独处理在 `target/platform-modules` 中
- 不同平台的多种格式图标（icns, ico, png）

**构建输出：**
- `target/runtime-image` - 自定义 JRE
- `target/app-image` - 自包含应用程序
- `target/dependencies` - 所有 JAR 依赖项

## 模块系统

应用程序使用 Java 平台模块系统（JPMS）：
- 模块名称：`com.xiebiao.tools.redisfx`
- 主类：`com.xiebiao.tools.redisfx.Launcher`
- 所有包在 module-info.java 中显式导出
- 控制器向 javafx.fxml 开放以进行反射访问
- JVM 选项中为 JavaFX 启用本机访问

## 开发说明

- 开发模式通过 `-Dredisfx.mode=dev` 系统属性激活（在 Maven 插件中设置）
- 语言环境硬编码为 en_US（代码中已标注 i18n TODO）
- 应用程序启动时最大化
- 选项卡面板动态调整大小（固定宽度选项卡的代码已注释）
- 使用 SplitPane，左侧边栏分隔符位于 0.195
