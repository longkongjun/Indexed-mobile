# Jikan API 模块

Jikan API 的 Kotlin Multiplatform 客户端实现，提供了完整的 MyAnimeList 数据访问接口。

## 📁 项目结构

```
jikan/
├── API.md                          # 完整的 API 接口文档
├── README.md                       # 本文件
├── build.gradle.kts               # 构建配置
├── src/
│   └── commonMain/
│       └── kotlin/
│           └── com/pusu/indexed/jikan/
│               ├── JikanApi.kt                    # 主 API 聚合接口
│               ├── JikanClient.kt                 # HTTP 客户端
│               ├── api/                           # API 接口定义
│               │   ├── AnimeApi.kt               # 动漫接口
│               │   ├── MangaApi.kt               # 漫画接口
│               │   ├── CharacterApi.kt           # 角色接口
│               │   ├── PeopleApi.kt              # 人物接口
│               │   ├── SeasonApi.kt              # 季度接口
│               │   ├── ProducerApi.kt            # 制作公司接口
│               │   ├── MagazineApi.kt            # 杂志接口
│               │   ├── ClubApi.kt                # 俱乐部接口
│               │   ├── UserApi.kt                # 用户接口
│               │   └── WatchApi.kt               # 观看推荐接口
│               ├── impl/                          # API 接口实现
│               │   ├── AnimeApiImpl.kt           # 动漫接口实现
│               │   ├── MangaApiImpl.kt           # 漫画接口实现
│               │   ├── CharacterApiImpl.kt       # 角色接口实现
│               │   ├── PeopleApiImpl.kt          # 人物接口实现
│               │   ├── SeasonApiImpl.kt          # 季度接口实现
│               │   ├── ProducerApiImpl.kt        # 制作公司接口实现
│               │   ├── MagazineApiImpl.kt        # 杂志接口实现
│               │   ├── ClubApiImpl.kt            # 俱乐部接口实现
│               │   ├── UserApiImpl.kt            # 用户接口实现
│               │   └── WatchApiImpl.kt           # 观看推荐接口实现
│               └── models/                       # 数据模型
│                   ├── anime/                    # 动漫相关模型
│                   │   └── Anime.kt
│                   ├── manga/                    # 漫画相关模型
│                   │   └── Manga.kt
│                   ├── character/                # 角色相关模型
│                   │   └── Character.kt
│                   ├── people/                   # 人物相关模型
│                   │   └── People.kt
│                   ├── season/                   # 季度相关模型
│                   │   └── Season.kt
│                   ├── producer/                 # 制作公司相关模型
│                   │   └── Producer.kt
│                   ├── magazine/                 # 杂志相关模型
│                   │   └── Magazine.kt
│                   ├── club/                     # 俱乐部相关模型
│                   │   └── Club.kt
│                   ├── user/                     # 用户相关模型
│                   │   └── User.kt
│                   ├── watch/                    # 观看推荐相关模型
│                   │   └── Watch.kt
│                   └── common/                   # 通用模型
│                       ├── CommonModels.kt
│                       ├── JikanResponse.kt
│                       └── JikanErrorResponse.kt
```

## 🚀 快速开始

### 安装

在项目的 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation(project(":shared:data:jikan"))
}
```

### 基本使用

```kotlin
import com.pusu.indexed.jikan.createJikanApi

// 创建 API 实例
val jikanApi = createJikanApi()

// 使用各个子 API
suspend fun example() {
    // 获取动漫信息
    jikanApi.anime.getAnimeById(1).fold(
        onSuccess = { response -> println(response.data.title) },
        onFailure = { error -> println("Error: ${error.message}") }
    )
    
    // 搜索漫画
    jikanApi.manga.searchManga(query = "One Piece")
    
    // 获取角色信息
    jikanApi.characters.getCharacterById(40)
    
    // 获取当前季度动漫
    jikanApi.seasons.getCurrentSeasonAnime()
    
    // 获取随机动漫
    jikanApi.random.getRandomAnime()
    
    // 获取排行榜
    jikanApi.top.getTopAnime(page = 1, limit = 10)
    
    // 获取类型列表
    jikanApi.genres.getAnimeGenres()
    
    // 获取播放时间表
    jikanApi.schedules.getSchedules(filter = "monday")
    
    // 获取最新推荐
    jikanApi.recommendations.getRecentAnimeRecommendations()
    
    // 获取最新评论
    jikanApi.reviews.getRecentAnimeReviews()
}
```

## 📚 API 分类

### 1. Anime API（动漫）
- 基础信息、完整信息查询
- 角色、工作人员、剧集列表
- 新闻、论坛、评论
- 视频、图片
- 统计数据、推荐、用户更新
- 搜索和排行榜

### 2. Manga API（漫画）
- 基础信息、完整信息查询
- 角色列表
- 新闻、论坛、评论
- 图片、统计数据
- 推荐、用户更新
- 搜索和排行榜

### 3. Character API（角色）
- 基础信息、完整信息查询
- 图片列表
- 搜索和排行榜

### 4. People API（人物）
- 人物（声优/制作人员）信息查询
- 图片列表
- 搜索和排行榜

### 5. Season API（季度）
- 指定季度动漫查询
- 当前季度动漫
- 即将播出动漫
- 所有季度列表

### 6. Producer API（制作公司）
- 制作公司信息查询
- 搜索制作公司

### 7. Magazine API（杂志）
- 杂志/出版社搜索

### 8. Club API（俱乐部）
- 俱乐部信息查询
- 成员、工作人员列表
- 关系信息
- 搜索俱乐部

### 9. User API（用户）
- 用户资料查询
- 统计、收藏、好友
- 历史记录
- 搜索用户

### 10. Watch API（观看推荐）
- 最新宣传视频
- 最新剧集视频
- 热门视频

### 11. Genres API（类型/题材）
- 动漫类型列表
- 漫画类型列表
- 类型过滤

### 12. Random API（随机）
- 随机动漫
- 随机漫画
- 随机角色
- 随机人物
- 随机用户

### 13. Recommendations API（推荐）
- 最新动漫推荐
- 最新漫画推荐

### 14. Reviews API（评论）
- 最新动漫评论
- 最新漫画评论

### 15. Schedules API（播放时间表）
- 当前季度播放时间表
- 按星期过滤

### 16. Top API（排行榜）
- 动漫排行榜
- 漫画排行榜
- 角色排行榜
- 人物排行榜
- 评论排行榜

## 🏗️ 架构设计

### 设计原则

1. **职责分离**：每个资源类型有独立的 API 接口和实现
2. **类型安全**：使用枚举类型代替魔法字符串
3. **一致性**：所有实现统一使用 `client.get()` + `buildMap` 模式
4. **空值处理**：在 API 层过滤 null 参数，不传递给底层
5. **文档完善**：每个方法都有详细的注释说明

### 错误处理

所有 API 方法返回 `Result<T>` 类型：
- 成功时包含 `JikanResponse<T>` 或 `JikanPageResponse<T>`
- 失败时包含异常信息

```kotlin
result.fold(
    onSuccess = { response -> /* 处理成功 */ },
    onFailure = { error -> /* 处理错误 */ }
)
```

### 分页响应

支持分页的接口返回 `JikanPageResponse<T>`：
```kotlin
data class JikanPageResponse<T>(
    val data: List<T>,
    val pagination: Pagination
)
```

## 🔧 配置选项

### 自定义 HTTP 客户端

```kotlin
val customClient = JikanClient(
    baseUrl = "https://api.jikan.moe/v4",
    httpClient = myHttpClient
)
val api = createJikanApi(customClient)
```

## 📖 完整文档

详细的 API 文档请查看 [API.md](./API.md)

## ⚠️ 注意事项

1. **请求限制**：
   - 3 请求/秒
   - 60 请求/分钟
   - 建议实现请求限流和缓存机制

2. **数据缓存**：
   - 对不经常变化的数据建议实现本地缓存
   - 减少 API 调用，提升用户体验

3. **错误重试**：
   - 网络请求可能失败，建议实现重试机制
   - 但要注意不要超过请求限制

## 🔗 相关链接

- [Jikan 官方文档](https://docs.api.jikan.moe/)
- [MyAnimeList](https://myanimelist.net/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)

## 📝 更新日志

### v1.1.0 (2025-01-01)
- ✨ 新增 6 个 API 接口模块
  - GenresApi - 类型/题材接口
  - RandomApi - 随机获取接口
  - RecommendationsApi - 全局推荐接口
  - ReviewsApi - 全局评论接口
  - SchedulesApi - 播放时间表接口
  - TopApi - 统一排行榜接口
- 📦 新增相关数据模型
- 📚 完善 API 文档
- 🏗️ 实现类统一移至 impl 目录

### v1.0.0 (2025-01-01)
- ✨ 初始版本发布
- ✅ 实现 10 个主要 API 接口
- 📚 完整的 API 文档
- 🏗️ 清晰的架构设计
- 🔒 类型安全的枚举和参数

---

**作者**: Indexed Team  
**许可证**: 见项目根目录 LICENSE 文件

