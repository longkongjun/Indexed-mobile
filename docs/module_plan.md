### 工程结构（KMM + Compose，多端）

```
root
├─ docs/                         # 文档
├─ core/                         # 核心基础设施（KMP）
│  ├─ network/                   # 网络层（Ktor 客户端）
│  └─ utils/                     # 纯 Kotlin 工具
├─ data/                         # 数据层（KMP）
│  └─ jikan/                     # Jikan API 数据源实现
├─ domain/                       # 业务逻辑层（KMP）
│  └─ anime/                     # 动漫业务领域（合并 discover + feed）
├─ feature/                      # 功能模块层（KMP + Compose）
│  ├─ discover/                  # 发现页功能（UI + ViewModel）
│  ├─ anime-detail/              # 动漫详情功能（UI + ViewModel）
│  └─ search/                    # 搜索功能（UI + ViewModel）
├─ app/                          # 应用主模块（多平台）
│  └─ src/
│     ├─ commonMain/             # 共享代码（导航、DI 等）
│     ├─ androidMain/            # Android 平台代码
│     ├─ iosMain/                # iOS 平台代码
│     ├─ jvmMain/                # Desktop 平台代码
│     └─ jsMain/                 # Web 平台代码
└─ gradle/ 等                    # 构建配置
```

### 分层职责
- **core**：最基础能力；`network` 提供跨平台网络客户端（Ktor）；`utils` 纯 Kotlin 工具，无平台依赖。
- **data**：数据源/仓库实现，当前 `jikan` 实现 Jikan API 数据访问，依赖 `core/network`。
- **domain**：业务接口与用例（纯 Kotlin），定义业务逻辑接口，不依赖平台和 UI。`anime` 模块包含所有动漫相关的业务逻辑（列表、搜索、详情等）。
- **feature**：功能模块（UI + ViewModel），使用 Compose Multiplatform 实现跨端 UI，依赖 `core`、`domain`、`data`。
- **app**：应用主模块，负责导航组装、依赖注入、平台特定配置，依赖所有业务模块。

### 依赖方向（摘要）
```
app
    ↓
feature (discover, anime-detail, search)
    ↓
domain (anime)
    ↓
data (jikan)
    ↓
core (network, utils)
```

**依赖规则**：
- `app` → `feature` → `domain` → `data` → `core`
- `feature` 可直接依赖 `data`（推荐经由 `domain` 接口注入）
- `core` 不依赖 `feature`/`domain`/`data`/`app`
- `domain` 不依赖 Android/Compose，保持纯 Kotlin
- `feature` 使用 `androidx.lifecycle.ViewModel` 和 Compose Multiplatform
- `domain/anime` 合并了原 `discover` 和 `feed` 模块，统一管理动漫业务逻辑

### 版本与构建要点
- KMP + Compose Multiplatform，Android namespace/compileSdk 在各模块 `build.gradle.kts` 中声明。
- Compose dev 仓库：`https://maven.pkg.jetbrains.space/public/p/compose/dev` 已在 `settings.gradle.kts`。

