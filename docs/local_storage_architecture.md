# 本地存储管理架构详解

## 目录

1. [概述](#概述)
2. [整体架构](#整体架构)
3. [核心组件](#核心组件)
4. [数据流与流程](#数据流与流程)
5. [类图与关系](#类图与关系)
6. [平台实现](#平台实现)
7. [使用示例](#使用示例)

---

## 概述

本地存储管理架构负责处理本地漫画库的完整生命周期，包括：

- **来源管理**：支持三种来源（下载、应用内导入、应用外导入）
- **文件扫描**：识别漫画、章节、页面结构
- **索引缓存**：构建可查询的数据库索引
- **元数据刮削**：从在线源获取封面和信息
- **后台同步**：自动化的后台任务调度

### 设计原则

1. **跨平台统一**：提供一致的 API，隐藏平台差异
2. **渐进增强**：支持增量扫描，避免重复工作
3. **任务编排**：清晰的阶段划分和错误恢复
4. **用户可控**：提供丰富的配置选项
5. **资源友好**：批量处理、约束条件、后台执行

---

## 整体架构

### 分层架构图

```mermaid
graph TB
    subgraph "Feature Layer 功能层"
        UI[Local Library UI<br/>本地库界面]
    end
    
    subgraph "Domain Layer 领域层"
        UC1[AddLibraryRootUseCase<br/>添加根目录]
        UC2[ScanLibraryUseCase<br/>扫描库]
        UC3[EnqueueScrapeUseCase<br/>入队刮削]
        
        subgraph "Domain Models 领域模型"
            LR[LibraryRoot<br/>根目录]
            COM[Comic/Chapter/Page<br/>漫画内容]
            TASK[ScanTask/ScrapeTask<br/>任务模型]
        end
    end
    
    subgraph "Data Layer 数据层"
        subgraph "Orchestration 编排层"
            ORCH[SyncOrchestrator<br/>同步编排器]
            IDX[IndexUpdateManager<br/>索引更新管理器]
        end
        
        subgraph "Core Services 核心服务"
            SCAN[LocalScanner<br/>扫描器]
            QUEUE[LocalScrapeQueue<br/>刮削队列]
            STORE[LocalLibraryRootStore<br/>根目录存储]
        end
        
        subgraph "Background 后台任务"
            SCHED[BackgroundTaskScheduler<br/>任务调度器]
            WORKER[BackgroundSyncWorker<br/>后台工作器]
        end
    end
    
    subgraph "Platform Layer 平台层"
        subgraph "Android"
            WM[WorkManager<br/>任务管理]
            SAF[SAF<br/>存储访问框架]
        end
        
        subgraph "iOS"
            BGT[BGTaskScheduler<br/>后台任务]
            FILES[Files API<br/>文件访问]
        end
        
        DB[(SQLDelight<br/>数据库)]
        FS[File System<br/>文件系统]
    end
    
    UI --> UC1 & UC2 & UC3
    UC1 & UC2 & UC3 --> LR & COM & TASK
    UC2 --> ORCH
    UC3 --> QUEUE
    ORCH --> SCAN & IDX & QUEUE
    IDX --> STORE & DB
    SCAN --> FS
    QUEUE --> DB
    STORE --> DB
    SCHED --> WM & BGT
    WORKER --> ORCH
    SCAN -.Android.-> SAF
    SCAN -.iOS.-> FILES
    
    style UI fill:#e1f5ff
    style ORCH fill:#fff4e1
    style SCAN fill:#e8f5e9
    style SCHED fill:#f3e5f5
    style DB fill:#fce4ec
```

### 模块依赖关系

```mermaid
graph LR
    subgraph "domain:comic"
        DM[Domain Models<br/>领域模型]
        UC[Use Cases<br/>用例]
        REPO[Repositories<br/>仓库接口]
    end
    
    subgraph "data:local"
        IMPL[Repository Impl<br/>仓库实现]
        SCANNER[LocalScanner<br/>扫描器]
        SCRAPER[LocalScrapeQueue<br/>刮削队列]
        ORCH2[SyncOrchestrator<br/>编排器]
        BG[Background Services<br/>后台服务]
    end
    
    subgraph "feature:local-library"
        FEAT[UI & ViewModel<br/>界面与视图模型]
    end
    
    FEAT --> UC
    UC --> DM
    UC --> REPO
    IMPL --> REPO
    IMPL --> DM
    SCANNER --> DM
    SCRAPER --> DM
    ORCH2 --> SCANNER & SCRAPER & IMPL
    BG --> ORCH2
    
    style DM fill:#bbdefb
    style UC fill:#c8e6c9
    style IMPL fill:#fff9c4
```

---

## 核心组件

### 1. LibraryRoot（库根目录）

代表一个本地漫画库的入口点，支持三种来源：

```mermaid
classDiagram
    class LibraryRoot {
        +String id
        +String displayName
        +String rootUri
        +LibraryRootSource source
        +LibraryRootPermission permission
        +Long lastScannedAtMillis
        +Int comicCount
        +Boolean autoSyncEnabled
        +isAvailable() Boolean
        +needsRescan() Boolean
        +updateScanInfo() LibraryRoot
    }
    
    class LibraryRootSource {
        <<enumeration>>
        DOWNLOADED
        IMPORTED_INTERNAL
        IMPORTED_EXTERNAL
    }
    
    class LibraryRootPermission {
        +Boolean canRead
        +Boolean canWrite
        +Boolean isPersisted
        +Long grantedAtMillis
        +Long lastVerifiedAtMillis
        +isValid() Boolean
        +updateVerified() LibraryRootPermission
    }
    
    LibraryRoot --> LibraryRootSource
    LibraryRoot --> LibraryRootPermission
    
    note for LibraryRootSource "DOWNLOADED: 应用内下载\nIMPORTED_INTERNAL: 应用内导入\nIMPORTED_EXTERNAL: 应用外导入"
```

**三种来源的区别**：

| 来源 | 存储位置 | 权限控制 | 平台差异 |
|------|---------|---------|---------|
| **DOWNLOADED** | 应用私有目录 | 完全控制 | 无 |
| **IMPORTED_INTERNAL** | 应用私有目录 | 完全控制 | 无 |
| **IMPORTED_EXTERNAL** | 外部存储 | 需要授权 | Android: SAF<br/>iOS: Files |

### 2. SyncOrchestrator（同步编排器）

协调扫描-索引-刮削的完整流程：

```mermaid
stateDiagram-v2
    [*] --> 验证权限
    验证权限 --> 扫描阶段: 权限有效
    验证权限 --> 失败: 权限无效
    
    扫描阶段 --> 索引更新阶段: 扫描完成
    扫描阶段 --> 失败: 扫描失败
    
    索引更新阶段 --> 刮削入队阶段: 更新完成
    索引更新阶段 --> 失败: 更新失败
    
    刮削入队阶段 --> 成功: 入队完成
    刮削入队阶段 --> 成功: 自动刮削禁用
    
    成功 --> [*]
    失败 --> [*]
    
    note right of 扫描阶段
        LocalScanner.scanRoot()
        - 遍历文件系统
        - 识别漫画/章节/页面
        - 返回 ScanResult
    end note
    
    note right of 索引更新阶段
        IndexUpdateManager.updateIndexBatch()
        - 对比现有索引
        - 增删改操作
        - 批量提交数据库
    end note
    
    note right of 刮削入队阶段
        LocalScrapeQueue.enqueueAll()
        - 为新增漫画创建任务
        - 优先级排序
        - 异步执行
    end note
```

### 3. LocalScanner（本地扫描器）

识别文件系统中的漫画结构：

```mermaid
flowchart TD
    Start([开始扫描]) --> CheckRoot{根目录<br/>可访问?}
    CheckRoot -->|否| Error1[返回错误]
    CheckRoot -->|是| ScanType{扫描类型?}
    
    ScanType -->|全量| FullScan[遍历所有文件]
    ScanType -->|增量| IncrScan[仅扫描变化]
    
    FullScan --> FindComics[识别漫画<br/>一级文件夹]
    IncrScan --> CheckModTime[检查修改时间]
    CheckModTime --> FindComics
    
    FindComics --> ForEachComic{遍历<br/>每个漫画}
    ForEachComic -->|下一个| FindChapters[识别章节<br/>子文件夹/CBZ]
    
    FindChapters --> ForEachChapter{遍历<br/>每个章节}
    ForEachChapter -->|下一个| FindPages[识别页面<br/>图片文件]
    
    FindPages --> FilterPages[过滤图片<br/>jpg/png/webp]
    FilterPages --> SortPages[自然排序<br/>文件名]
    SortPages --> ForEachChapter
    
    ForEachChapter -->|完成| ForEachComic
    ForEachComic -->|完成| BuildResult[构建结果]
    
    BuildResult --> Result([返回 ScanResult])
    Error1 --> End([结束])
    Result --> End
    
    style Start fill:#c8e6c9
    style Result fill:#c8e6c9
    style Error1 fill:#ffcdd2
    style End fill:#e0e0e0
```

**扫描规则**：

1. **漫画识别**：根目录下的一级文件夹
2. **章节识别**：漫画文件夹下的子文件夹或 `.cbz` 文件
3. **页面识别**：章节内的图片文件（支持格式：jpg, jpeg, png, webp）
4. **排序规则**：按文件名自然排序（如：`001.jpg` < `002.jpg` < `10.jpg`）
5. **过滤规则**：忽略隐藏文件（`.` 开头）

### 4. IndexUpdateManager（索引更新管理器）

执行增量索引更新：

```mermaid
flowchart TD
    Start([接收扫描结果]) --> LoadExisting[从数据库加载<br/>现有索引]
    
    LoadExisting --> Compare[对比差异]
    
    Compare --> CalcNew[计算新增项]
    Compare --> CalcUpdate[计算更新项]
    Compare --> CalcDelete[计算删除项]
    
    CalcNew --> BatchNew[分批处理新增]
    CalcUpdate --> BatchUpdate[分批处理更新]
    CalcDelete --> BatchDelete[分批处理删除]
    
    BatchNew --> InsertComics[插入漫画]
    InsertComics --> InsertChapters[插入章节]
    InsertChapters --> InsertPages[插入页面]
    
    BatchUpdate --> UpdateComics[更新漫画]
    UpdateComics --> UpdateChapters[更新章节]
    UpdateChapters --> UpdatePages[更新页面]
    
    BatchDelete --> DeletePages[删除页面]
    DeletePages --> DeleteChapters[级联删除章节]
    DeleteChapters --> DeleteComics[级联删除漫画]
    
    InsertPages --> CleanOrphans[清理孤立数据]
    UpdatePages --> CleanOrphans
    DeleteComics --> CleanOrphans
    
    CleanOrphans --> Stats[统计结果]
    Stats --> Result([返回 UpdateResult])
    
    style Start fill:#c8e6c9
    style Result fill:#c8e6c9
    style CleanOrphans fill:#fff9c4
```

**更新策略**：

- **新增**：`scanned_uri` 在扫描结果中存在，但数据库中不存在
- **更新**：`scanned_uri` 在两边都存在，但 `updated_at` 不同
- **删除**：`db_uri` 在数据库中存在，但扫描结果中不存在

### 5. LocalScrapeQueue（刮削队列）

管理元数据获取任务：

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant Queue as LocalScrapeQueue
    participant DB as 数据库
    participant Executor as 后台执行器
    participant API as 在线 API
    
    Client->>Queue: enqueue(comicId, title)
    Queue->>Queue: 检查去重
    
    alt 已存在待处理任务
        Queue-->>Client: 返回现有任务 ID
    else 新任务
        Queue->>DB: 创建 ScrapeTask
        Queue-->>Client: 返回新任务 ID
    end
    
    loop 后台执行
        Executor->>Queue: dequeue()
        Queue->>DB: 获取最高优先级任务
        DB-->>Queue: ScrapeTask
        Queue-->>Executor: 返回任务
        
        Executor->>API: 搜索/获取元数据
        
        alt 成功
            API-->>Executor: 元数据
            Executor->>DB: 更新漫画信息
            Executor->>Queue: markCompleted()
            Queue->>DB: 更新任务状态
        else 失败
            API-->>Executor: 错误
            alt 可重试
                Executor->>Queue: markFailedWithRetry()
                Queue->>DB: 重新入队（retryCount++）
            else 达到最大重试次数
                Executor->>Queue: markFailed()
                Queue->>DB: 标记失败
            end
        end
    end
```

**任务优先级**：

1. **用户手动触发**：priority = 100
2. **新增漫画（自动）**：priority = 50
3. **重试任务**：priority = 原优先级 - 10

**重试策略**：

- 最大重试次数：3 次
- 失败后重新入队
- 每次重试降低优先级

---

## 数据流与流程

### 完整同步流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant UI as UI 层
    participant UC as ScanLibraryUseCase
    participant Orch as SyncOrchestrator
    participant Scanner as LocalScanner
    participant IndexMgr as IndexUpdateManager
    participant Queue as LocalScrapeQueue
    participant DB as 数据库
    participant FS as 文件系统
    
    User->>UI: 点击"刷新"
    UI->>UC: scanRoot(rootId, INCREMENTAL)
    UC->>Orch: syncRoot(rootId, config)
    
    Note over Orch: 阶段 1: 扫描
    Orch->>DB: 获取 LibraryRoot
    DB-->>Orch: LibraryRoot
    Orch->>Orch: 验证权限
    
    Orch->>Scanner: scanRoot(root, INCREMENTAL)
    Scanner->>FS: 遍历文件系统
    FS-->>Scanner: 文件列表
    Scanner->>Scanner: 识别漫画/章节/页面
    Scanner-->>Orch: ScanResult(comics, chapters, pages)
    
    Note over Orch: 阶段 2: 索引更新
    Orch->>IndexMgr: updateIndexBatch(scanResult)
    IndexMgr->>DB: 加载现有索引
    DB-->>IndexMgr: 现有数据
    IndexMgr->>IndexMgr: 对比差异
    IndexMgr->>DB: 批量更新（增删改）
    IndexMgr-->>Orch: UpdateResult(统计)
    
    Note over Orch: 阶段 3: 刮削入队
    Orch->>Queue: enqueueAll(newComics)
    Queue->>DB: 创建 ScrapeTask
    Queue-->>Orch: 任务 ID 列表
    
    Orch->>DB: 更新 LibraryRoot.lastScannedAt
    Orch-->>UC: SyncResult
    UC-->>UI: Result(success)
    UI->>User: 显示结果
    
    Note over Queue,DB: 异步执行刮削
    Queue->>DB: dequeue()
    DB-->>Queue: ScrapeTask
    Note over Queue: 后台执行器处理...
```

### 添加根目录流程

```mermaid
flowchart TD
    Start([用户选择目录]) --> PlatformAuth{平台授权}
    
    PlatformAuth -->|Android| SAFPicker[SAF 文件选择器]
    PlatformAuth -->|iOS| FilesPicker[Files 选择器]
    
    SAFPicker --> PersistPerm[持久化 URI 权限]
    FilesPicker --> CopyToSandbox{导入方式?}
    
    CopyToSandbox -->|复制| CopyFiles[复制到沙盒]
    CopyToSandbox -->|引用| BookmarkURL[创建安全书签]
    
    PersistPerm --> CreateRoot[创建 LibraryRoot]
    CopyFiles --> CreateRoot
    BookmarkURL --> CreateRoot
    
    CreateRoot --> SetSource{设置来源类型}
    
    SetSource -->|下载| SourceDL[DOWNLOADED]
    SetSource -->|应用内| SourceInternal[IMPORTED_INTERNAL]
    SetSource -->|应用外| SourceExternal[IMPORTED_EXTERNAL]
    
    SourceDL --> SetPerm[设置权限]
    SourceInternal --> SetPerm
    SourceExternal --> SetPerm
    
    SetPerm --> SaveDB[保存到数据库]
    SaveDB --> TriggerScan[触发初始扫描]
    TriggerScan --> End([完成])
    
    style Start fill:#c8e6c9
    style End fill:#c8e6c9
    style CreateRoot fill:#fff9c4
```

### 增量扫描优化流程

```mermaid
flowchart TD
    Start([开始增量扫描]) --> LoadLastScan[加载上次扫描时间]
    
    LoadLastScan --> ScanRoot[遍历根目录]
    
    ScanRoot --> CheckComic{检查漫画<br/>目录}
    
    CheckComic --> GetModTime[获取修改时间]
    GetModTime --> Compare{修改时间 ><br/>上次扫描?}
    
    Compare -->|未变化| Skip[跳过此漫画]
    Compare -->|有变化| FullScanComic[完整扫描此漫画]
    
    FullScanComic --> ScanChapters[扫描所有章节]
    ScanChapters --> ScanPages[扫描所有页面]
    
    ScanPages --> AddToResult[添加到结果]
    Skip --> NextComic{还有<br/>漫画?}
    AddToResult --> NextComic
    
    NextComic -->|是| CheckComic
    NextComic -->|否| ReturnResult([返回扫描结果])
    
    style Start fill:#c8e6c9
    style ReturnResult fill:#c8e6c9
    style Skip fill:#e0e0e0
```

---

## 类图与关系

### 领域模型关系

```mermaid
classDiagram
    class LibraryRoot {
        +String id
        +String displayName
        +String rootUri
        +LibraryRootSource source
        +LibraryRootPermission permission
        +Long lastScannedAtMillis
        +Int comicCount
    }
    
    class Comic {
        +String id
        +String title
        +String rootUri
        +String libraryRootId
        +String coverPageUri
        +Long updatedAtMillis
        +String sortKey
        +Int chapterCount
    }
    
    class Chapter {
        +String id
        +String comicId
        +String title
        +String chapterUri
        +Boolean isCbz
        +Long updatedAtMillis
        +String sortKey
        +Int pageCount
    }
    
    class Page {
        +String id
        +String chapterId
        +String fileName
        +Int pageIndex
        +String pageUri
        +Int? width
        +Int? height
    }
    
    class ScanTask {
        +String id
        +String libraryRootId
        +ScanType scanType
        +TaskStatus status
        +Int progress
        +Int foundComicCount
    }
    
    class ScrapeTask {
        +String id
        +String comicId
        +String comicTitle
        +ScrapeType scrapeType
        +TaskStatus status
        +Int priority
        +Int retryCount
    }
    
    LibraryRoot "1" --> "*" Comic : contains
    Comic "1" --> "*" Chapter : contains
    Chapter "1" --> "*" Page : contains
    LibraryRoot "1" --> "*" ScanTask : scanned by
    Comic "1" --> "*" ScrapeTask : scraped by
    
    note for LibraryRoot "根目录\n管理来源和权限"
    note for Comic "漫画\n一级文件夹"
    note for Chapter "章节\n子文件夹或 CBZ"
    note for Page "页面\n图片文件"
```

### 服务层关系

```mermaid
classDiagram
    class SyncOrchestrator {
        +syncRoot(rootId, scanType, config) SyncResult
        +syncAllRoots(scanType, config) List~SyncResult~
        +cancelSync(rootId)
    }
    
    class LocalScanner {
        +scanRoot(root, scanType, callback) ScanResult
        +scanComic(comicUri, rootId) ScanResult
        +verifyUri(uri) Boolean
    }
    
    class IndexUpdateManager {
        +updateIndexBatch(rootId, scanned) UpdateResult
        +deleteRootIndex(rootId)
        +cleanupOrphans()
    }
    
    class LocalScrapeQueue {
        +enqueue(comicId, title, type, priority) String
        +dequeue() ScrapeTask?
        +updateTask(task)
        +retryFailed()
    }
    
    class LibraryRootRepository {
        <<interface>>
        +getAllRoots() List~LibraryRoot~
        +getRootById(id) LibraryRoot?
        +addRoot(root)
        +updateRoot(root)
    }
    
    SyncOrchestrator --> LocalScanner : uses
    SyncOrchestrator --> IndexUpdateManager : uses
    SyncOrchestrator --> LocalScrapeQueue : uses
    SyncOrchestrator --> LibraryRootRepository : uses
    
    IndexUpdateManager --> LibraryRootRepository : uses
```

### 后台任务调度

```mermaid
classDiagram
    class BackgroundTaskScheduler {
        <<expect/actual>>
        +scheduleSyncTask(interval, constraints)
        +scheduleOneTimeSyncTask(delay, constraints)
        +cancelAllSyncTasks()
    }
    
    class BackgroundSyncWorker {
        <<interface>>
        +doWork() WorkResult
        +shouldStop() Boolean
    }
    
    class TaskConstraints {
        +Boolean requiresNetwork
        +NetworkType networkType
        +Boolean requiresCharging
        +Boolean requiresBatteryNotLow
    }
    
    class AndroidScheduler {
        -WorkManager workManager
        +scheduleSyncTask()
        +scheduleOneTimeSyncTask()
    }
    
    class iOSScheduler {
        +scheduleSyncTask()
        +scheduleOneTimeSyncTask()
    }
    
    BackgroundTaskScheduler <|.. AndroidScheduler : implements
    BackgroundTaskScheduler <|.. iOSScheduler : implements
    BackgroundTaskScheduler --> TaskConstraints : uses
    BackgroundSyncWorker --> SyncOrchestrator : calls
    
    note for AndroidScheduler "使用 WorkManager\n最小间隔 15 分钟\n支持丰富约束"
    note for iOSScheduler "使用 BGTaskScheduler\n系统决定执行时间\n约束有限"
```

---

## 平台实现

### Android 平台特性

```mermaid
graph TB
    subgraph "Android 实现"
        subgraph "文件访问"
            SAF[Storage Access Framework]
            DF[DocumentFile API]
            PP[Persisted Permissions]
        end
        
        subgraph "后台任务"
            WM[WorkManager]
            PWR[PeriodicWorkRequest]
            OWR[OneTimeWorkRequest]
            CONS[Constraints Builder]
        end
        
        subgraph "权限管理"
            PERM[运行时权限]
            BATT[电池优化豁免]
            DOZE[Doze 模式兼容]
        end
    end
    
    SAF --> DF
    DF --> PP
    
    WM --> PWR
    WM --> OWR
    PWR --> CONS
    OWR --> CONS
    
    CONS --> PERM
    CONS --> BATT
    BATT --> DOZE
    
    style SAF fill:#a5d6a7
    style WM fill:#90caf9
    style PERM fill:#ffcc80
```

**关键实现点**：

1. **SAF 文件访问**：
   ```kotlin
   // 请求目录访问
   val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
   startActivityForResult(intent, REQUEST_CODE)
   
   // 持久化权限
   contentResolver.takePersistableUriPermission(
       uri,
       Intent.FLAG_GRANT_READ_URI_PERMISSION or
       Intent.FLAG_GRANT_WRITE_URI_PERMISSION
   )
   ```

2. **WorkManager 调度**：
   ```kotlin
   val workRequest = PeriodicWorkRequestBuilder<LocalSyncWorker>(
       1, TimeUnit.HOURS
   )
       .setConstraints(
           Constraints.Builder()
               .setRequiredNetworkType(NetworkType.CONNECTED)
               .setRequiresBatteryNotLow(true)
               .build()
       )
       .build()
   
   WorkManager.getInstance(context)
       .enqueueUniquePeriodicWork(
           "local_sync",
           ExistingPeriodicWorkPolicy.KEEP,
           workRequest
       )
   ```

### iOS 平台特性

```mermaid
graph TB
    subgraph "iOS 实现"
        subgraph "文件访问"
            FILES[Files App Integration]
            BOOKMARK[Security-Scoped Bookmark]
            SANDBOX[App Sandbox]
        end
        
        subgraph "后台任务"
            BGT[BGTaskScheduler]
            REFRESH[BGAppRefreshTask]
            PROCESS[BGProcessingTask]
        end
        
        subgraph "限制"
            USAGE[应用使用习惯]
            LOWPOWER[低电量模式]
            FORCEQUIT[强制退出限制]
        end
    end
    
    FILES --> BOOKMARK
    BOOKMARK --> SANDBOX
    
    BGT --> REFRESH
    BGT --> PROCESS
    
    REFRESH -.受限于.-> USAGE
    REFRESH -.受限于.-> LOWPOWER
    REFRESH -.受限于.-> FORCEQUIT
    PROCESS -.受限于.-> USAGE
    PROCESS -.受限于.-> LOWPOWER
    PROCESS -.受限于.-> FORCEQUIT
    
    style FILES fill:#a5d6a7
    style BGT fill:#90caf9
    style LOWPOWER fill:#ffcc80
```

**关键实现点**：

1. **Files 集成**：
   ```swift
   // 文档选择器
   let picker = UIDocumentPickerViewController(
       forOpeningContentTypes: [.folder]
   )
   present(picker, animated: true)
   
   // 创建安全书签
   let bookmarkData = try url.bookmarkData(
       options: .minimalBookmark,
       includingResourceValuesForKeys: nil,
       relativeTo: nil
   )
   ```

2. **BGTaskScheduler**：
   ```swift
   // Info.plist 配置
   <key>BGTaskSchedulerPermittedIdentifiers</key>
   <array>
       <string>com.pusu.indexed.comics.refresh</string>
   </array>
   
   // 注册任务
   BGTaskScheduler.shared.register(
       forTaskWithIdentifier: "com.pusu.indexed.comics.refresh",
       using: nil
   ) { task in
       handleRefresh(task: task as! BGAppRefreshTask)
   }
   
   // 调度任务
   let request = BGAppRefreshTaskRequest(
       identifier: "com.pusu.indexed.comics.refresh"
   )
   request.earliestBeginDate = Date(timeIntervalSinceNow: 3600)
   try BGTaskScheduler.shared.submit(request)
   ```

---

## 使用示例

### 1. 添加根目录

```kotlin
// 用户选择目录后
val libraryRoot = LibraryRoot.createExternalImportedRoot(
    id = calculateUriHash(selectedUri),
    displayName = "我的漫画",
    rootUri = selectedUri,
    permission = LibraryRootPermission.externalGrant(
        canRead = true,
        canWrite = true,
        isPersisted = true
    )
)

// 添加到仓库
addLibraryRootUseCase(libraryRoot)

// 触发初始扫描
scanLibraryUseCase.scanRoot(
    libraryRootId = libraryRoot.id,
    scanType = ScanType.FULL
)
```

### 2. 手动刷新

```kotlin
// UI 层触发
viewModelScope.launch {
    _uiState.value = UiState.Loading
    
    val result = scanLibraryUseCase.scanRoot(
        libraryRootId = currentRootId,
        scanType = ScanType.INCREMENTAL
    )
    
    _uiState.value = when {
        result.success -> UiState.Success(
            newComics = result.newComicCount,
            updated = result.updatedComicCount
        )
        else -> UiState.Error(result.error ?: "未知错误")
    }
}
```

### 3. 配置自动同步

```kotlin
// 调度周期性任务
val scheduler = BackgroundTaskScheduler()
scheduler.scheduleSyncTask(
    intervalMinutes = 60, // 每小时
    constraints = TaskConstraints(
        requiresNetwork = false,
        requiresBatteryNotLow = true
    )
)

// 禁用某个根目录的自动同步
libraryRootRepository.updateRoot(
    currentRoot.copy(autoSyncEnabled = false)
)
```

### 4. 手动触发刮削

```kotlin
// 为单个漫画获取元数据
val taskId = enqueueScrapeUseCase.enqueueForComic(
    comicId = comic.id,
    comicTitle = comic.title,
    scrapeType = ScrapeType.FULL,
    priority = 100 // 高优先级
)

// 批量刮削
val comics = listOf(
    "comic1" to "标题1",
    "comic2" to "标题2"
)
enqueueScrapeUseCase.enqueueForComics(comics)
```

### 5. 监听同步进度

```kotlin
// 使用回调监听进度
val callback = object : SyncOrchestrator.SyncProgressCallback {
    override suspend fun onScanStarted(libraryRootId: String) {
        println("开始扫描: $libraryRootId")
    }
    
    override suspend fun onScanProgress(progress: Int, currentItem: String) {
        println("扫描进度: $progress% - $currentItem")
    }
    
    override suspend fun onIndexingStarted(totalItems: Int) {
        println("开始索引更新，共 $totalItems 项")
    }
    
    override suspend fun onIndexingProgress(processed: Int, total: Int) {
        println("索引进度: $processed/$total")
    }
    
    override suspend fun onScrapeStarted(taskCount: Int) {
        println("刮削任务已入队: $taskCount 个")
    }
    
    override suspend fun onSyncCompleted(result: SyncResult) {
        println("同步完成: 新增 ${result.newComicCount} 个漫画")
    }
    
    override suspend fun onSyncFailed(error: String) {
        println("同步失败: $error")
    }
}

// 执行同步
syncOrchestrator.syncRoot(
    libraryRootId = rootId,
    scanType = ScanType.INCREMENTAL,
    callback = callback
)
```

---

## 总结

本架构提供了完整的本地漫画库管理方案：

### ✅ 核心特性

1. **多来源支持**：下载、导入、外部存储
2. **智能扫描**：全量/增量、进度追踪
3. **自动索引**：增删改检测、批量更新
4. **元数据刮削**：优先级队列、自动重试
5. **后台同步**：跨平台调度、约束控制

### 📊 性能优化

- 增量扫描减少 I/O
- 批量提交避免频繁数据库操作
- 异步刮削不阻塞主流程
- 智能调度节省资源

### 🔧 可扩展性

- 清晰的分层架构
- 接口驱动设计
- 平台差异隔离
- 易于测试和维护

### 🎯 下一步

1. 实现平台特定代码（Android/iOS）
2. 集成 SQLDelight 数据库
3. 开发 UI 功能模块
4. 添加单元测试和集成测试
