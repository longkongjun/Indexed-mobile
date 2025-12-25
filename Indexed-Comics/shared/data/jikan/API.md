# Jikan API 接口文档

本文档详细说明了 Jikan API Kotlin 客户端的所有接口。

## 📋 目录

- [概述](#概述)
- [快速开始](#快速开始)
- [Anime API - 动漫接口](#anime-api---动漫接口)
- [Manga API - 漫画接口](#manga-api---漫画接口)
- [Character API - 角色接口](#character-api---角色接口)
- [People API - 人物接口](#people-api---人物接口)
- [Season API - 季度接口](#season-api---季度接口)
- [Producer API - 制作公司接口](#producer-api---制作公司接口)
- [Magazine API - 杂志接口](#magazine-api---杂志接口)
- [Club API - 俱乐部接口](#club-api---俱乐部接口)
- [User API - 用户接口](#user-api---用户接口)
- [Watch API - 观看推荐接口](#watch-api---观看推荐接口)
- [错误处理](#错误处理)
- [最佳实践](#最佳实践)

## 概述

Jikan API 是一个非官方的 MyAnimeList (MAL) REST API，提供动漫、漫画、角色等数据的访问接口。

- **官方文档**: https://docs.api.jikan.moe/
- **Base URL**: `https://api.jikan.moe/v4`
- **请求限制**: 3 请求/秒，60 请求/分钟

## 快速开始

### 创建 API 实例

```kotlin
// 使用默认配置
val jikanApi = createJikanApi()

// 使用自定义配置
val customClient = JikanClient(
    baseUrl = "https://api.jikan.moe/v4",
    httpClient = myCustomHttpClient
)
val jikanApi = createJikanApi(customClient)
```

### 基本用法示例

```kotlin
// 获取动漫信息
val result = jikanApi.anime.getAnimeById(id = 1)
result.fold(
    onSuccess = { response ->
        println("动漫标题: ${response.data.title}")
    },
    onFailure = { error ->
        println("请求失败: ${error.message}")
    }
)

// 搜索漫画
val searchResult = jikanApi.manga.searchManga(
    query = "One Piece",
    page = 1,
    limit = 10
)
```

---

## Anime API - 动漫接口

提供动漫相关的所有接口，包括基础信息、角色、工作人员、剧集、新闻等。

### 基础信息查询

#### `getAnimeById(id: Int)`

获取单个动漫的基础信息。

- **路径**: `GET /anime/{id}`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<Anime>>`

```kotlin
val result = jikanApi.anime.getAnimeById(id = 1)
```

#### `getAnimeFullById(id: Int)`

获取单个动漫的完整信息（包含关系、主题曲等）。

- **路径**: `GET /anime/{id}/full`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<Anime>>`

```kotlin
val result = jikanApi.anime.getAnimeFullById(id = 1)
```

### 角色与工作人员

#### `getAnimeCharacters(id: Int)`

获取动漫的角色列表。

- **路径**: `GET /anime/{id}/characters`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<List<AnimeCharacter>>>`

```kotlin
val result = jikanApi.anime.getAnimeCharacters(id = 1)
```

#### `getAnimeStaff(id: Int)`

获取动漫的工作人员列表。

- **路径**: `GET /anime/{id}/staff`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<List<AnimeStaff>>>`

```kotlin
val result = jikanApi.anime.getAnimeStaff(id = 1)
```

### 剧集信息

#### `getAnimeEpisodes(id: Int, page: Int?)`

获取动漫的剧集列表。

- **路径**: `GET /anime/{id}/episodes`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `page`: 页码（可选，从 1 开始）
- **返回**: `Result<JikanPageResponse<AnimeEpisode>>`

```kotlin
val result = jikanApi.anime.getAnimeEpisodes(id = 1, page = 1)
```

#### `getAnimeEpisodeById(id: Int, episode: Int)`

获取动漫的单个剧集信息。

- **路径**: `GET /anime/{id}/episodes/{episode}`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `episode`: 集数（必需）
- **返回**: `Result<JikanResponse<AnimeEpisode>>`

```kotlin
val result = jikanApi.anime.getAnimeEpisodeById(id = 1, episode = 1)
```

### 社区内容

#### `getAnimeNews(id: Int, page: Int?)`

获取动漫的新闻列表。

- **路径**: `GET /anime/{id}/news`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `page`: 页码（可选）
- **返回**: `Result<JikanPageResponse<AnimeNews>>`

#### `getAnimeForum(id: Int, filter: String?)`

获取动漫的论坛话题列表。

- **路径**: `GET /anime/{id}/forum`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `filter`: 过滤条件（可选）：`all`, `episode`, `other`
- **返回**: `Result<JikanResponse<List<ForumTopic>>>`

#### `getAnimeReviews(id: Int, page: Int?)`

获取动漫的评论列表。

- **路径**: `GET /anime/{id}/reviews`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `page`: 页码（可选）
- **返回**: `Result<JikanPageResponse<AnimeReview>>`

### 多媒体内容

#### `getAnimeVideos(id: Int)`

获取动漫的视频列表（预告、剧集、MV）。

- **路径**: `GET /anime/{id}/videos`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<AnimeVideos>>`

#### `getAnimePictures(id: Int)`

获取动漫的图片列表。

- **路径**: `GET /anime/{id}/pictures`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<List<Picture>>>`

### 统计与推荐

#### `getAnimeStatistics(id: Int)`

获取动漫的统计数据。

- **路径**: `GET /anime/{id}/statistics`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<AnimeStatistics>>`

#### `getAnimeRecommendations(id: Int)`

获取动漫的推荐列表。

- **路径**: `GET /anime/{id}/recommendations`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<List<Recommendation>>>`

#### `getAnimeUserUpdates(id: Int, page: Int?)`

获取动漫的用户更新列表。

- **路径**: `GET /anime/{id}/userupdates`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
  - `page`: 页码（可选）
- **返回**: `Result<JikanPageResponse<UserUpdate>>`

### 其他信息

#### `getAnimeMoreInfo(id: Int)`

获取动漫的更多信息。

- **路径**: `GET /anime/{id}/moreinfo`
- **参数**:
  - `id`: MAL 动漫 ID（必需）
- **返回**: `Result<JikanResponse<String>>`

### 搜索与排行

#### `searchAnime(...)`

搜索动漫。

- **路径**: `GET /anime`
- **参数**:
  - `query`: 搜索关键词（可选）
  - `page`: 页码（可选，从 1 开始）
  - `limit`: 每页数量（可选，最大 25）
  - `type`: 动漫类型（可选）：`tv`, `movie`, `ova`, `special`, `ona`, `music`
  - `score`: 最低评分（可选）
  - `status`: 状态（可选）：`airing`, `complete`, `upcoming`
  - `rating`: 分级（可选）：`g`, `pg`, `pg13`, `r17`, `r`, `rx`
  - `sfw`: 是否仅显示安全内容（可选）
  - `genres`: 类型 ID 列表，逗号分隔（可选）
  - `orderBy`: 排序字段（可选）
  - `sort`: 排序方向（可选）：`asc`, `desc`
- **返回**: `Result<JikanPageResponse<Anime>>`

```kotlin
val result = jikanApi.anime.searchAnime(
    query = "Naruto",
    type = "tv",
    status = "complete",
    orderBy = "score",
    sort = "desc"
)
```

#### `getTopAnime(page: Int?, limit: Int?, filter: AnimeFilter?)`

获取 Top 动漫排行榜。

- **路径**: `GET /top/anime`
- **参数**:
  - `page`: 页码（可选）
  - `limit`: 每页数量（可选）
  - `filter`: 过滤条件（可选）：`BY_POPULARITY`, `AIRING`, `UPCOMING`, `FAVORITE`
- **返回**: `Result<JikanPageResponse<Anime>>`

```kotlin
val result = jikanApi.anime.getTopAnime(
    page = 1,
    limit = 10,
    filter = AnimeFilter.BY_POPULARITY
)
```

---

## Manga API - 漫画接口

提供漫画相关的所有接口。

### 基础信息查询

#### `getMangaById(id: Int)`

获取单个漫画的基础信息。

- **路径**: `GET /manga/{id}`
- **参数**:
  - `id`: MAL 漫画 ID（必需）
- **返回**: `Result<JikanResponse<Manga>>`

#### `getMangaFullById(id: Int)`

获取单个漫画的完整信息。

- **路径**: `GET /manga/{id}/full`
- **参数**:
  - `id`: MAL 漫画 ID（必需）
- **返回**: `Result<JikanResponse<Manga>>`

### 角色信息

#### `getMangaCharacters(id: Int)`

获取漫画的角色列表。

- **路径**: `GET /manga/{id}/characters`
- **参数**:
  - `id`: MAL 漫画 ID（必需）
- **返回**: `Result<JikanResponse<List<MangaCharacter>>>`

### 社区内容

#### `getMangaNews(id: Int, page: Int?)`

获取漫画的新闻列表。

- **路径**: `GET /manga/{id}/news`

#### `getMangaForum(id: Int, filter: String?)`

获取漫画的论坛话题列表。

- **路径**: `GET /manga/{id}/forum`

#### `getMangaReviews(id: Int, page: Int?)`

获取漫画的评论列表。

- **路径**: `GET /manga/{id}/reviews`

### 多媒体与统计

#### `getMangaPictures(id: Int)`

获取漫画的图片列表。

- **路径**: `GET /manga/{id}/pictures`

#### `getMangaStatistics(id: Int)`

获取漫画的统计数据。

- **路径**: `GET /manga/{id}/statistics`

### 其他信息

#### `getMangaMoreInfo(id: Int)`

获取漫画的更多信息。

- **路径**: `GET /manga/{id}/moreinfo`

#### `getMangaRecommendations(id: Int)`

获取漫画的推荐列表。

- **路径**: `GET /manga/{id}/recommendations`

#### `getMangaUserUpdates(id: Int, page: Int?)`

获取漫画的用户更新列表。

- **路径**: `GET /manga/{id}/userupdates`

### 搜索与排行

#### `searchManga(...)`

搜索漫画。

- **路径**: `GET /manga`
- **参数**: 类似 `searchAnime`，但类型选项不同
  - `type`: `manga`, `novel`, `lightnovel`, `oneshot`, `doujin`, `manhwa`, `manhua`
  - `status`: `publishing`, `complete`, `upcoming`

```kotlin
val result = jikanApi.manga.searchManga(
    query = "One Piece",
    type = "manga",
    status = "publishing"
)
```

#### `getTopManga(page: Int?, limit: Int?, filter: MangaFilter?)`

获取 Top 漫画排行榜。

- **路径**: `GET /top/manga`
- **过滤条件**: `BY_POPULARITY`, `PUBLISHING`, `UPCOMING`, `FAVORITE`

---

## Character API - 角色接口

提供角色相关的接口。

### 基础信息查询

#### `getCharacterById(id: Int)`

获取角色的基础信息。

- **路径**: `GET /characters/{id}`
- **参数**:
  - `id`: MAL 角色 ID（必需）
- **返回**: `Result<JikanResponse<Character>>`

#### `getCharacterFullById(id: Int)`

获取角色的完整信息（包含动漫、漫画、声优）。

- **路径**: `GET /characters/{id}/full`

#### `getCharacterPictures(id: Int)`

获取角色的图片列表。

- **路径**: `GET /characters/{id}/pictures`

### 搜索与排行

#### `searchCharacters(...)`

搜索角色。

- **路径**: `GET /characters`
- **参数**:
  - `query`: 搜索关键词
  - `page`: 页码
  - `limit`: 每页数量
  - `orderBy`: `mal_id`, `name`, `favorites`
  - `sort`: `asc`, `desc`

#### `getTopCharacters(page: Int?, limit: Int?)`

获取 Top 角色排行榜。

- **路径**: `GET /top/characters`

---

## People API - 人物接口

提供人物（声优/制作人员）相关的接口。

### 基础信息查询

#### `getPersonById(id: Int)`

获取人物的基础信息。

- **路径**: `GET /people/{id}`

#### `getPersonFullById(id: Int)`

获取人物的完整信息。

- **路径**: `GET /people/{id}/full`

#### `getPersonPictures(id: Int)`

获取人物的图片列表。

- **路径**: `GET /people/{id}/pictures`

### 搜索与排行

#### `searchPeople(...)`

搜索人物。

- **路径**: `GET /people`
- **排序字段**: `mal_id`, `name`, `birthday`, `favorites`

#### `getTopPeople(page: Int?, limit: Int?)`

获取 Top 人物排行榜。

- **路径**: `GET /top/people`

---

## Season API - 季度接口

提供季度动漫相关的接口。

### 季度查询

#### `getSeasonAnime(year: Int, season: String, page: Int?, limit: Int?)`

获取指定季度的动漫列表。

- **路径**: `GET /seasons/{year}/{season}`
- **参数**:
  - `year`: 年份，例如 2023（必需）
  - `season`: 季度（必需）：`winter`, `spring`, `summer`, `fall`
  - `page`: 页码（可选）
  - `limit`: 每页数量（可选）

```kotlin
val result = jikanApi.seasons.getSeasonAnime(
    year = 2023,
    season = "fall",
    page = 1
)
```

#### `getCurrentSeasonAnime(page: Int?, limit: Int?)`

获取当前季度的动漫列表。

- **路径**: `GET /seasons/now`

#### `getUpcomingSeasonAnime(page: Int?, limit: Int?)`

获取即将播出的季度动漫列表。

- **路径**: `GET /seasons/upcoming`

#### `getAllSeasons()`

获取所有可用的季度列表。

- **路径**: `GET /seasons`

---

## Producer API - 制作公司接口

提供制作公司相关的接口。

### 基础信息查询

#### `getProducerById(id: Int)`

获取制作公司的基础信息。

- **路径**: `GET /producers/{id}`

#### `getProducerFullById(id: Int)`

获取制作公司的完整信息。

- **路径**: `GET /producers/{id}/full`

### 搜索

#### `searchProducers(...)`

搜索制作公司。

- **路径**: `GET /producers`
- **排序字段**: `mal_id`, `name`, `count`, `favorites`, `established`

---

## Magazine API - 杂志接口

提供杂志/出版社相关的接口。

### 搜索

#### `searchMagazines(...)`

搜索杂志/出版社。

- **路径**: `GET /magazines`
- **排序字段**: `mal_id`, `name`, `count`

---

## Club API - 俱乐部接口

提供俱乐部相关的接口。

### 基础信息查询

#### `getClubById(id: Int)`

获取俱乐部的基础信息。

- **路径**: `GET /clubs/{id}`

#### `getClubMembers(id: Int, page: Int?)`

获取俱乐部的成员列表。

- **路径**: `GET /clubs/{id}/members`

#### `getClubStaff(id: Int)`

获取俱乐部的工作人员列表。

- **路径**: `GET /clubs/{id}/staff`

#### `getClubRelations(id: Int)`

获取俱乐部的关系（相关动漫、漫画、角色）。

- **路径**: `GET /clubs/{id}/relations`

### 搜索

#### `searchClubs(...)`

搜索俱乐部。

- **路径**: `GET /clubs`
- **参数**:
  - `type`: `public`, `private`, `secret`
  - `category`: `anime`, `manga`, `actors_and_artists`, `characters`, 等
  - `orderBy`: `mal_id`, `name`, `members_count`, `pictures_count`, `created`

---

## User API - 用户接口

提供用户相关的接口。

### 基础信息查询

#### `getUserByUsername(username: String)`

获取用户的基础信息。

- **路径**: `GET /users/{username}`

#### `getUserFullProfile(username: String)`

获取用户的完整资料。

- **路径**: `GET /users/{username}/full`

#### `getUserStatistics(username: String)`

获取用户的统计信息。

- **路径**: `GET /users/{username}/statistics`

#### `getUserFavorites(username: String)`

获取用户的收藏。

- **路径**: `GET /users/{username}/favorites`

#### `getUserFriends(username: String, page: Int?)`

获取用户的好友列表。

- **路径**: `GET /users/{username}/friends`

#### `getUserHistory(username: String, type: String?)`

获取用户的历史记录。

- **路径**: `GET /users/{username}/history`
- **参数**:
  - `type`: `anime`, `manga`

### 搜索

#### `searchUsers(...)`

搜索用户。

- **路径**: `GET /users`
- **参数**:
  - `gender`: `any`, `male`, `female`, `nonbinary`
  - `location`: 所在地
  - `maxAge`: 最大年龄
  - `minAge`: 最小年龄

---

## Watch API - 观看推荐接口

提供最新宣传视频和剧集视频的接口。

### 最新视频

#### `getRecentPromoVideos(page: Int?)`

获取最新的宣传视频列表。

- **路径**: `GET /watch/promos`

#### `getRecentEpisodeVideos(page: Int?)`

获取最新的剧集视频列表。

- **路径**: `GET /watch/episodes`

### 热门视频

#### `getPopularPromoVideos(page: Int?, limit: Int?)`

获取热门宣传视频列表。

- **路径**: `GET /watch/promos/popular`

#### `getPopularEpisodeVideos(page: Int?, limit: Int?)`

获取热门剧集视频列表。

- **路径**: `GET /watch/episodes/popular`

---

## 错误处理

所有 API 方法返回 `Result<T>` 类型，可以使用 Kotlin 的 Result API 进行错误处理。

### 基本错误处理

```kotlin
val result = jikanApi.anime.getAnimeById(1)
result.fold(
    onSuccess = { response ->
        // 处理成功情况
        println(response.data.title)
    },
    onFailure = { error ->
        // 处理失败情况
        println("Error: ${error.message}")
    }
)
```

### 高级错误处理

```kotlin
val result = jikanApi.anime.searchAnime(query = "Naruto")
when {
    result.isSuccess -> {
        val response = result.getOrNull()
        println("找到 ${response?.data?.size} 个结果")
    }
    result.isFailure -> {
        val exception = result.exceptionOrNull()
        when (exception) {
            is IOException -> println("网络错误")
            is SerializationException -> println("数据解析错误")
            else -> println("未知错误: ${exception?.message}")
        }
    }
}
```

---

## 最佳实践

### 1. 遵守请求限制

Jikan API 有请求限制：
- **3 请求/秒**
- **60 请求/分钟**

建议实现请求限流机制，避免被限流。

### 2. 缓存数据

对于不经常变化的数据（如动漫基础信息、角色信息），建议实现本地缓存。

```kotlin
class CachedJikanRepository(
    private val api: JikanApi,
    private val cache: Cache
) {
    suspend fun getAnime(id: Int): Result<Anime> {
        // 先检查缓存
        cache.get(id)?.let { return Result.success(it) }
        
        // 缓存未命中，请求 API
        return api.anime.getAnimeById(id).map { response ->
            response.data.also { cache.put(id, it) }
        }
    }
}
```

### 3. 使用协程进行并发请求

```kotlin
suspend fun loadAnimeDetails(ids: List<Int>) = coroutineScope {
    ids.map { id ->
        async { jikanApi.anime.getAnimeById(id) }
    }.awaitAll()
}
```

### 4. 错误重试策略

```kotlin
suspend fun <T> retryRequest(
    times: Int = 3,
    delay: Long = 1000,
    block: suspend () -> Result<T>
): Result<T> {
    repeat(times - 1) {
        val result = block()
        if (result.isSuccess) return result
        delay(delay)
    }
    return block()
}

// 使用
val result = retryRequest {
    jikanApi.anime.getAnimeById(1)
}
```

### 5. 分页数据加载

```kotlin
suspend fun loadAllAnimePages(query: String): List<Anime> {
    val allAnime = mutableListOf<Anime>()
    var page = 1
    
    while (true) {
        val result = jikanApi.anime.searchAnime(
            query = query,
            page = page,
            limit = 25
        ).getOrNull() ?: break
        
        allAnime.addAll(result.data)
        
        // 检查是否还有更多页
        if (!result.pagination.hasNextPage) break
        page++
        
        // 尊重请求限制
        delay(350) // 约 3 请求/秒
    }
    
    return allAnime
}
```

---

## 相关资源

- **Jikan 官方文档**: https://docs.api.jikan.moe/
- **MyAnimeList**: https://myanimelist.net/
- **GitHub Issues**: 如有问题，请在项目 GitHub 上提交 Issue

---

**最后更新**: 2025-01-01  
**API 版本**: v4  
**文档版本**: 1.0.0

