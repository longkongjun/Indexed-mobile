package com.pusu.indexed.jikan.impl

import com.pusu.indexed.jikan.JikanClient
import com.pusu.indexed.jikan.api.AnimeApi
import com.pusu.indexed.jikan.api.AnimeFilter
import com.pusu.indexed.jikan.models.anime.*
import com.pusu.indexed.jikan.models.common.*

/**
 * AnimeApi 的默认实现。
 * 
 * 职责：
 * - 将接口方法调用转换为对应的 HTTP 请求路径和参数
 * - 委托 JikanClient 执行实际的网络请求
 * - 不包含业务逻辑，仅负责参数组装和过滤
 * 
 * @property client HTTP 客户端，负责底层网络通信
 */
internal class AnimeApiImpl(
    private val client: JikanClient
) : AnimeApi {
    
    override suspend fun getAnimeById(id: Int): Result<JikanResponse<Anime>> =
        client.get(path = listOf("anime", id.toString()))
    
    override suspend fun getAnimeFullById(id: Int): Result<JikanResponse<Anime>> =
        client.get(path = listOf("anime", id.toString(), "full"))
    
    override suspend fun getAnimeCharacters(id: Int): Result<JikanResponse<List<AnimeCharacter>>> =
        client.get(path = listOf("anime", id.toString(), "characters"))
    
    override suspend fun getAnimeStaff(id: Int): Result<JikanResponse<List<AnimeStaff>>> =
        client.get(path = listOf("anime", id.toString(), "staff"))
    
    override suspend fun getAnimeEpisodes(
        id: Int,
        page: Int?
    ): Result<JikanPageResponse<AnimeEpisode>> =
        client.get(
            path = listOf("anime", id.toString(), "episodes"),
            query = buildMap {
                page?.let { put("page", it) }
            }
        )
    
    override suspend fun getAnimeEpisodeById(
        id: Int,
        episode: Int
    ): Result<JikanResponse<AnimeEpisode>> =
        client.get(path = listOf("anime", id.toString(), "episodes", episode.toString()))
    
    override suspend fun getAnimeNews(
        id: Int,
        page: Int?
    ): Result<JikanPageResponse<AnimeNews>> =
        client.get(
            path = listOf("anime", id.toString(), "news"),
            query = buildMap {
                page?.let { put("page", it) }
            }
        )
    
    override suspend fun getAnimeForum(
        id: Int,
        filter: String?
    ): Result<JikanResponse<List<ForumTopic>>> =
        client.get(
            path = listOf("anime", id.toString(), "forum"),
            query = buildMap {
                filter?.let { put("filter", it) }
            }
        )
    
    override suspend fun getAnimeVideos(id: Int): Result<JikanResponse<AnimeVideos>> =
        client.get(path = listOf("anime", id.toString(), "videos"))
    
    override suspend fun getAnimePictures(id: Int): Result<JikanResponse<List<Picture>>> =
        client.get(path = listOf("anime", id.toString(), "pictures"))
    
    override suspend fun getAnimeStatistics(id: Int): Result<JikanResponse<AnimeStatistics>> =
        client.get(path = listOf("anime", id.toString(), "statistics"))
    
    override suspend fun getAnimeMoreInfo(id: Int): Result<JikanResponse<String>> =
        client.get(path = listOf("anime", id.toString(), "moreinfo"))
    
    override suspend fun getAnimeRecommendations(id: Int): Result<JikanResponse<List<Recommendation>>> =
        client.get(path = listOf("anime", id.toString(), "recommendations"))
    
    override suspend fun getAnimeUserUpdates(
        id: Int,
        page: Int?
    ): Result<JikanPageResponse<UserUpdate>> =
        client.get(
            path = listOf("anime", id.toString(), "userupdates"),
            query = buildMap {
                page?.let { put("page", it) }
            }
        )
    
    override suspend fun getAnimeReviews(
        id: Int,
        page: Int?
    ): Result<JikanPageResponse<AnimeReview>> =
        client.get(
            path = listOf("anime", id.toString(), "reviews"),
            query = buildMap {
                page?.let { put("page", it) }
            }
        )
    
    override suspend fun searchAnime(
        query: String?,
        page: Int?,
        limit: Int?,
        type: String?,
        score: Double?,
        status: String?,
        rating: String?,
        sfw: Boolean?,
        genres: String?,
        orderBy: String?,
        sort: String?
    ): Result<JikanPageResponse<Anime>> =
        client.get(
            path = listOf("anime"),
            query = buildMap {
                query?.let { put("q", it) }
                page?.let { put("page", it) }
                limit?.let { put("limit", it) }
                type?.let { put("type", it) }
                score?.let { put("score", it) }
                status?.let { put("status", it) }
                rating?.let { put("rating", it) }
                sfw?.let { put("sfw", it) }
                genres?.let { put("genres", it) }
                orderBy?.let { put("order_by", it) }
                sort?.let { put("sort", it) }
            }
        )
    
    override suspend fun getTopAnime(
        page: Int?,
        limit: Int?,
        filter: AnimeFilter?
    ): Result<JikanPageResponse<Anime>> =
        client.get(
            path = listOf("top", "anime"),
            query = buildMap {
                page?.let { put("page", it) }
                limit?.let { put("limit", it) }
                filter?.let { put("filter", it.value) }
            }
        )
}

