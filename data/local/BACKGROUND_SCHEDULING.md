# 后台任务调度方案

## 概述

本文档描述 Android 和 iOS 平台的后台任务调度实现方案，用于自动同步本地漫画库。

## 设计目标

1. **跨平台统一接口**：提供一致的调度 API
2. **尊重平台特性**：遵循各平台的后台任务最佳实践
3. **用户友好**：不过度消耗电量和流量
4. **可靠性**：保证任务最终执行
5. **灵活性**：支持多种约束条件

## 跨平台抽象

### BackgroundTaskScheduler

统一的后台任务调度器接口（expect/actual）：

```kotlin
expect class BackgroundTaskScheduler {
    fun scheduleSyncTask(intervalMinutes: Long, constraints: TaskConstraints)
    fun scheduleOneTimeSyncTask(delayMinutes: Long, constraints: TaskConstraints)
    fun cancelAllSyncTasks()
    fun cancelTask(taskId: String)
    fun isTaskScheduled(taskId: String): Boolean
}
```

### TaskConstraints

统一的任务约束配置：

```kotlin
data class TaskConstraints(
    val requiresNetwork: Boolean,
    val networkType: NetworkType,
    val requiresCharging: Boolean,
    val requiresDeviceIdle: Boolean,
    val requiresBatteryNotLow: Boolean
)
```

预定义约束：
- **DEFAULT**：常规同步（无网络要求，电池不低）
- **RELAXED**：宽松约束（尽快执行）
- **STRICT**：严格约束（仅充电且有 Wi-Fi）
- **SCRAPING**：刮削任务（需要网络）

## Android 实现

### 技术选型：WorkManager

#### 特性

1. **保证执行**：即使应用关闭或设备重启
2. **智能调度**：系统资源允许时执行
3. **约束支持**：网络、充电、电池、空闲等
4. **自动重试**：支持指数退避
5. **Doze 兼容**：在 Doze 模式下仍可执行

#### 任务类型

1. **PeriodicWorkRequest**：周期性任务
   - 最小间隔：15 分钟
   - 用于定期同步
   - 系统自动重复执行

2. **OneTimeWorkRequest**：一次性任务
   - 立即或延迟执行
   - 用于用户手动触发或特定事件

#### 约束映射

| TaskConstraints | WorkManager Constraints |
|----------------|------------------------|
| requiresNetwork | setRequiredNetworkType(CONNECTED) |
| networkType.UNMETERED | setRequiredNetworkType(UNMETERED) |
| requiresCharging | setRequiresCharging(true) |
| requiresDeviceIdle | setRequiresDeviceIdle(true) |
| requiresBatteryNotLow | setRequiresBatteryNotLow(true) |

#### 实现示例

```kotlin
class LocalSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // 获取依赖
            val orchestrator = getSyncOrchestrator()
            
            // 执行同步
            val results = orchestrator.syncAllRoots(
                scanType = ScanType.INCREMENTAL,
                config = SyncOrchestrator.SyncConfig(
                    enableAutoScrape = true
                )
            )
            
            // 记录结果
            logSyncResults(results)
            
            Result.success()
        } catch (e: Exception) {
            // 重试逻辑
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
```

#### 调度示例

```kotlin
// 周期性同步（每 1 小时）
val workRequest = PeriodicWorkRequestBuilder<LocalSyncWorker>(
    1, TimeUnit.HOURS
)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    )
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        15, TimeUnit.MINUTES
    )
    .build()

workManager.enqueueUniquePeriodicWork(
    "local_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    workRequest
)
```

#### Doze 模式处理

Android Doze 模式会限制后台活动，但 WorkManager 会在维护窗口执行：

1. **标准 Doze**：设备静止一段时间进入
   - 维护窗口：约 1 小时一次
   - WorkManager 在窗口期执行

2. **App Standby**：应用长时间未使用
   - 分为 5 个级别（Active → Rare）
   - 限制后台任务频率

3. **应对策略**：
   - 使用 `setRequiresDeviceIdle(false)` 避免过度限制
   - 结合前台触发保证及时性
   - 重要任务可使用 `setExpedited()` 加快执行

#### 电池优化处理

用户可能将应用列入电池优化白名单，需要引导：

```kotlin
fun requestBatteryOptimizationExemption(context: Context) {
    if (!isIgnoringBatteryOptimizations(context)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}
```

### 配置要求

#### AndroidManifest.xml

```xml
<!-- WorkManager 需要的权限 -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- 可选：请求电池优化豁免 -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

## iOS 实现

### 技术选型：BGTaskScheduler

#### 特性

1. **应用刷新**：短时间后台执行（约 30 秒）
2. **后台处理**：较长时间执行（几分钟）
3. **智能调度**：系统根据使用习惯决定执行时间
4. **省电优化**：低电量模式下不执行

#### 任务类型

1. **BGAppRefreshTask**：应用刷新任务
   - 执行时间：约 30 秒
   - 适用场景：轻量级扫描
   - 调度频率：系统决定，通常在用户常用时间段
   - 优势：更频繁执行

2. **BGProcessingTask**：后台处理任务
   - 执行时间：几分钟
   - 适用场景：重量级任务（刮削）
   - 调度时机：设备空闲时
   - 约束：可要求网络、充电

#### iOS 限制

1. **不保证执行**：
   - 系统根据应用使用习惯决定
   - 用户强制退出应用后不执行
   - 低电量模式下暂停

2. **频率限制**：
   - 刷新任务：每天数次（取决于使用频率）
   - 处理任务：更少（通常在夜间充电时）

3. **需要定期使用**：
   - 长期不使用的应用会被降低优先级
   - 需要用户偶尔打开应用

#### 约束映射

| TaskConstraints | BGTaskRequest |
|----------------|---------------|
| requiresNetwork | requiresNetworkConnectivity |
| requiresCharging | requiresExternalPower |
| 其他约束 | 不支持（忽略） |

#### 实现步骤

##### 1. Info.plist 配置

```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.pusu.indexed.comics.refresh</string>
    <string>com.pusu.indexed.comics.processing</string>
</array>
```

##### 2. 应用启动时注册

```swift
// AppDelegate.swift 或 App.swift
func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
) -> Bool {
    registerBackgroundTasks()
    return true
}

func registerBackgroundTasks() {
    // 注册刷新任务
    BGTaskScheduler.shared.register(
        forTaskWithIdentifier: "com.pusu.indexed.comics.refresh",
        using: nil
    ) { task in
        self.handleAppRefresh(task: task as! BGAppRefreshTask)
    }
    
    // 注册处理任务
    BGTaskScheduler.shared.register(
        forTaskWithIdentifier: "com.pusu.indexed.comics.processing",
        using: nil
    ) { task in
        self.handleProcessing(task: task as! BGProcessingTask)
    }
}
```

##### 3. 调度任务

```swift
func scheduleRefresh() {
    let request = BGAppRefreshTaskRequest(
        identifier: "com.pusu.indexed.comics.refresh"
    )
    
    // 建议最早开始时间（不保证）
    request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60) // 1 小时后
    
    do {
        try BGTaskScheduler.shared.submit(request)
    } catch {
        print("Could not schedule refresh: \(error)")
    }
}
```

##### 4. 处理任务

```swift
func handleAppRefresh(task: BGAppRefreshTask) {
    // 1. 调度下一次刷新（模拟周期性）
    scheduleRefresh()
    
    // 2. 设置过期处理
    task.expirationHandler = {
        syncJob?.cancel()
    }
    
    // 3. 执行同步
    let syncJob = Task {
        do {
            let orchestrator = getSyncOrchestrator()
            _ = try await orchestrator.syncAllRoots(
                scanType: .incremental,
                config: .init(enableAutoScrape: false)
            )
            task.setTaskCompleted(success: true)
        } catch {
            task.setTaskCompleted(success: false)
        }
    }
}
```

#### 测试后台任务

使用 Xcode 的 lldb 命令测试：

```bash
# 模拟启动任务
e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateLaunchForTaskWithIdentifier:@"com.pusu.indexed.comics.refresh"]

# 模拟过期
e -l objc -- (void)[[BGTaskScheduler sharedScheduler] _simulateExpirationForTaskWithIdentifier:@"com.pusu.indexed.comics.refresh"]
```

### 配置要求

#### Info.plist

```xml
<key>BGTaskSchedulerPermittedIdentifiers</key>
<array>
    <string>com.pusu.indexed.comics.refresh</string>
    <string>com.pusu.indexed.comics.processing</string>
</array>

<!-- 可选：后台模式（如果需要） -->
<key>UIBackgroundModes</key>
<array>
    <string>processing</string>
</array>
```

## 调度策略

### 场景 1：常规自动同步

**目标**：定期扫描本地库，发现新增内容

**Android**：
- 任务类型：PeriodicWorkRequest
- 间隔：1-3 小时
- 约束：DEFAULT（无网络要求，电池不低）

**iOS**：
- 任务类型：BGAppRefreshTask
- 建议间隔：1 小时（实际由系统决定）
- 约束：无

### 场景 2：刮削任务

**目标**：获取在线元数据，需要网络

**Android**：
- 任务类型：PeriodicWorkRequest
- 间隔：6-12 小时
- 约束：SCRAPING（需要网络，电池不低）

**iOS**：
- 任务类型：BGProcessingTask
- 建议间隔：12 小时
- 约束：requiresNetworkConnectivity

### 场景 3：夜间重度同步

**目标**：利用夜间充电时间执行完整同步

**Android**：
- 任务类型：PeriodicWorkRequest
- 间隔：24 小时
- 约束：STRICT（充电 + Wi-Fi）

**iOS**：
- 任务类型：BGProcessingTask
- 建议间隔：24 小时
- 约束：requiresExternalPower + requiresNetworkConnectivity

### 场景 4：用户手动触发

**目标**：立即执行同步

**Android**：
- 任务类型：OneTimeWorkRequest
- 延迟：0 分钟
- 约束：RELAXED（无约束）

**iOS**：
- 任务类型：前台执行（不使用后台任务）

## 最佳实践

### 1. 电量友好

- 使用 `requiresBatteryNotLow` 避免低电量时执行
- 避免过于频繁的调度
- 增量扫描优先于全量扫描

### 2. 网络友好

- 刮削任务使用 `requiresNetwork`
- 大量数据使用 `UNMETERED`（仅 Wi-Fi）
- 提供用户设置：仅 Wi-Fi / 任何网络

### 3. 可靠性保障

- 结合前台触发（进入页面时）
- 后台任务作为补充，不作为唯一同步方式
- 错误重试机制

### 4. 用户体验

- 提供手动刷新按钮
- 显示上次同步时间
- 同步进度通知（可选）
- 允许用户控制同步频率

### 5. 监控与日志

- 记录任务执行时间
- 记录同步结果（成功/失败）
- 记录错误详情
- 统计后台任务执行频率

## 用户设置

建议提供以下设置项：

```kotlin
data class SyncSettings(
    // 是否启用自动同步
    val autoSyncEnabled: Boolean = true,
    
    // 同步频率（小时）
    val syncIntervalHours: Int = 3,
    
    // 网络要求
    val networkRequirement: NetworkRequirement = NetworkRequirement.ANY,
    
    // 是否仅在充电时同步
    val syncOnlyWhenCharging: Boolean = false,
    
    // 是否启用自动刮削
    val autoScrapeEnabled: Boolean = true
)

enum class NetworkRequirement {
    NONE,      // 无需网络（仅扫描）
    ANY,       // 任何网络
    UNMETERED  // 仅 Wi-Fi
}
```

## 调试工具

### Android

```kotlin
// 触发立即执行（调试用）
WorkManager.getInstance(context)
    .enqueueUniqueWork(
        "local_sync_debug",
        ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequest.from(LocalSyncWorker::class.java)
    )
```

### iOS

```swift
// 在开发阶段使用 _simulate 方法测试
// 生产环境不可用
```

## 迁移与升级

### 从旧版本迁移

如果之前使用其他后台任务方案（如 JobScheduler、AlarmManager），需要：

1. 取消旧的任务
2. 迁移配置到新系统
3. 调度新任务

### 版本兼容性

- Android：最低 API 23（Android 6.0）
- iOS：最低 iOS 13.0

## 总结

| 特性 | Android (WorkManager) | iOS (BGTaskScheduler) |
|-----|----------------------|----------------------|
| 保证执行 | ✅ 是 | ⚠️ 尽力而为 |
| 周期性任务 | ✅ 原生支持 | ⚠️ 需手动重调度 |
| 最小间隔 | 15 分钟 | 无限制（实际由系统决定） |
| 约束条件 | 丰富（网络、充电、电池、空闲） | 有限（网络、充电） |
| Doze 兼容 | ✅ 是 | N/A |
| 测试工具 | ✅ 完善 | ⚠️ 有限 |
| 执行时长 | 10 分钟+ | 30 秒（刷新）或几分钟（处理） |

**建议策略**：
- 前台触发为主，保证及时性
- 后台任务为辅，提供便利性
- Android 可依赖后台任务
- iOS 需结合用户使用习惯
