# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Redisfx is a Redis GUI tool built with JavaFX 22, targeting JDK 21. It provides a graphical interface for managing Redis connections, viewing server information, and manipulating keys.

**Technology Stack:**
- Java 21 with Java Modules (module-info.java)
- JavaFX 22 for UI
- AtlantaFX 2.0.1 for theming (PrimerLight/PrimerDark themes)
- Jedis 5.0.0 for Redis client
- Guava EventBus for internal event handling
- SQLite for local data persistence
- Maven for build management

## Common Commands

### Running the Application
```bash
# Run in development mode
mvn clean javafx:run

# Compile the project
mvn clean compile

# Run tests
mvn test

# Package application (creates platform-specific installer)
mvn clean install

# Package for specific platform
mvn clean install -P mac    # macOS
mvn clean install -P unix   # Linux
mvn clean install -P win    # Windows
```

### Development
```bash
# Clean build artifacts
mvn clean

# Copy dependencies
mvn dependency:copy-dependencies

# Create runtime image only
mvn package
```

## Architecture

### Application Structure

**Entry Point Flow:**
1. `Launcher.main()` → `RedisfxApplication.main()` → `RedisfxApplication.start()`
2. Main FXML loaded: `views/main-view.fxml` → `MainController`
3. Application uses a modular architecture (defined in `module-info.java`)

**Core Packages:**
- `com.xiebiao.tools.redisfx` - Main application class, launcher
- `com.xiebiao.tools.redisfx.controller` - FXML controllers (MainController, ConnectionInfoController)
- `com.xiebiao.tools.redisfx.view` - Custom JavaFX views (ConnectionTitledPane, KeyTabView, ServerInfoTabView, ToastView, MemoryMonitorView, StatisticsView)
- `com.xiebiao.tools.redisfx.model` - Data models (RedisConnectionInfo, RedisInfo, KeyPage, DetailInfo)
- `com.xiebiao.tools.redisfx.service.eventbus` - Event bus implementation using Guava
- `com.xiebiao.tools.redisfx.utils` - Utilities (Constants, Utils, Icons, RedisfxStyles, RedisCommand, RedisKeyTypes)
- `com.xiebiao.tools.redisfx.core` - Core interfaces (LifeCycle)

### Event Bus Architecture

The application uses a centralized event bus pattern via `RedisfxEventBusService` (wraps Guava EventBus):
- Controllers/views publish events with `RedisfxEventBusService.post(RedisfxEventMessasge)`
- Components register/unregister with `RedisfxEventBusService.register(this)` / `unregister(this)`
- Use `@Subscribe` annotation on handler methods
- Event types defined in `RedisfxEventType` enum
- Example: `NEW_CONNECTION_CREATED` event triggers UI updates in MainController

### Threading Model

- Main JavaFX application thread for UI operations
- `RedisfxApplication.mainThreadPool` - ThreadPoolExecutor (5-10 threads) for background tasks
- Use `Platform.runLater()` to update UI from background threads
- Each connection has scheduled executors for auto-refresh features

### Connection Management

**ConnectionTitledPane** is the core component for managing Redis connections:
- Creates and manages JedisPool for each connection
- Displays databases as tabs (ServerInfoTab, KeyInfoTab)
- Handles database selection via ChoiceBox
- Implements LifeCycle interface for cleanup
- Key-value operations performed through KeyTabView
- Server monitoring through ServerInfoTabView

### View Pattern

Custom views extend JavaFX components and implement creation methods:
- Views are composed programmatically (not FXML-based)
- Most views have a `create()` method returning the component
- Views subscribe to event bus for cross-component communication
- Views implement LifeCycle interface when managing resources

### FXML Views

Limited FXML usage for main layouts:
- `main-view.fxml` → MainController (main window with split pane)
- `connectioninfo-view.fxml` → ConnectionInfoController (connection dialog)
- `connection-view.fxml` (if exists)

## Key Implementation Patterns

### Adding a New Redis Operation
1. Add command constants to `RedisCommand` if needed
2. Implement operation in KeyTabView or create new view
3. Use JedisPool from ConnectionTitledPane
4. Wrap Jedis operations in try-with-resources
5. Post event via RedisfxEventBusService if other components need to react
6. Update UI on Platform.runLater() if called from background thread

### Creating a New View
1. Create class in `com.xiebiao.tools.redisfx.view` package
2. Implement LifeCycle if managing resources
3. Add `create()` method returning JavaFX node
4. Register with event bus if needed: `RedisfxEventBusService.register(this)`
5. Clean up in destroy(): unregister from event bus, close resources
6. Export package in module-info.java if needed

### Theme Support
- Application uses AtlantaFX themes (PrimerLight, PrimerDark, Primitive)
- Theme switching handled in MainController via menu items
- Custom styles defined in RedisfxStyles
- Use AtlantaFX style classes (Styles.BUTTON_*) for consistent theming

### Resource Management
- Components implementing LifeCycle must clean up in destroy()
- MainController.destroy() cascades to all ConnectionTitledPane instances
- JedisPools must be closed on connection removal
- EventBus listeners must be unregistered

## Build Configuration

The Maven build uses jlink and jpackage to create self-contained applications:
- **jlink** creates custom JRE with only required modules
- **jpackage** bundles application with JRE into platform-specific package
- Dependencies copied to `target/dependencies`
- JavaFX modules handled separately in `target/platform-modules`
- Icons in multiple formats for different platforms (icns, ico, png)

**Build Output:**
- `target/runtime-image` - Custom JRE
- `target/app-image` - Self-contained application
- `target/dependencies` - All JAR dependencies

## Module System

Application uses Java Platform Module System (JPMS):
- Module name: `com.xiebiao.tools.redisfx`
- Main class: `com.xiebiao.tools.redisfx.Launcher`
- All packages explicitly exported in module-info.java
- Controllers opened to javafx.fxml for reflection access
- Native access enabled for JavaFX in JVM options

## Development Notes

- Dev mode activated via `-Dredisfx.mode=dev` system property (set in Maven plugin)
- Locale hardcoded to en_US (i18n TODO noted in code)
- Application maximized on startup
- Tab pane dynamically sized (commented code for fixed-width tabs)
- Uses SplitPane with divider at 0.195 for left sidebar
