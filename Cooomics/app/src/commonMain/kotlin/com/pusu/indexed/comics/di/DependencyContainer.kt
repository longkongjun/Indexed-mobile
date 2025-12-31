package com.pusu.indexed.comics.di

import com.pusu.indexed.data.jikan.mapper.JikanToDiscoverMapper
import com.pusu.indexed.data.jikan.mapper.JikanToAnimeDetailMapper
import com.pusu.indexed.data.jikan.repository.JikanAnimeRepository
import com.pusu.indexed.data.jikan.repository.JikanAnimeDetailRepository
import com.pusu.indexed.data.jikan.repository.JikanRelatedAnimeRepository
import com.pusu.indexed.data.jikan.repository.JikanAnimeRecommendationsRepository
import com.pusu.indexed.domain.anime.repository.AnimeRepository
import com.pusu.indexed.domain.anime.usecase.GetTrendingAnimeUseCase
import com.pusu.indexed.domain.anime.usecase.GetCurrentSeasonAnimeUseCase
import com.pusu.indexed.domain.anime.usecase.GetTopRankedAnimeUseCase
import com.pusu.indexed.domain.anime.usecase.SearchAnimeUseCase
import com.pusu.indexed.domain.anime.usecase.GetAnimeDetailUseCase
import com.pusu.indexed.domain.anime.usecase.GetRelatedAnimeUseCase
import com.pusu.indexed.domain.anime.usecase.GetAnimeRecommendationsUseCase
import com.pusu.indexed.jikan.JikanApi
import com.pusu.indexed.jikan.JikanClient
import com.pusu.indexed.jikan.createJikanApi
import com.pusu.indexed.shared.feature.discover.presentation.DiscoverViewModel
import com.pusu.indexed.shared.feature.animedetail.presentation.AnimeDetailViewModel
import com.pusu.indexed.shared.feature.search.presentation.SearchViewModel
import com.pusu.indexed.shared.feature.animedetail.animelist.presentation.AnimeListViewModel
import com.pusu.indexed.shared.feature.animedetail.animelist.presentation.AnimeListType
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope

/**
 * 应用的依赖注入容器
 * 
 * 负责创建和管理所有依赖关系。
 * 这是一个简单的手动 DI 实现，不依赖第三方框架。
 * 
 * 依赖链：
 * HttpClient → JikanApi → Repository → UseCase → ViewModel
 */
class DependencyContainer(
    httpClient: HttpClient
) {
    // ========================================
    // Core 层：网络配置
    // ========================================
    
    private val httpClient: HttpClient = httpClient
    
    // ========================================
    // Data 层：API 和 Repository
    // ========================================
    
    /**
     * Jikan 客户端（封装了 HttpClient）
     */
    private val jikanClient: JikanClient by lazy {
        JikanClient(
            baseUrl = "https://api.jikan.moe/v4",
            httpClient = httpClient
        )
    }
    
    /**
     * Jikan API 客户端
     */
    private val jikanApi: JikanApi by lazy {
        createJikanApi(jikanClient)
    }
    
    /**
     * Jikan 到 Discover 的数据转换器
     */
    private val jikanToDiscoverMapper: JikanToDiscoverMapper by lazy {
        JikanToDiscoverMapper()
    }
    
    /**
     * Jikan 到 AnimeDetail 的数据转换器
     */
    private val jikanToAnimeDetailMapper: JikanToAnimeDetailMapper by lazy {
        JikanToAnimeDetailMapper()
    }
    
    /**
     * Discover Repository 实现
     */
    private val discoverRepository: AnimeRepository by lazy {
        JikanAnimeRepository(
            jikanApi = jikanApi,
            mapper = jikanToDiscoverMapper
        )
    }
    
    // ========================================
    // Domain 层：UseCase
    // ========================================
    
    /**
     * 获取热门动漫的 UseCase
     */
    private val getTrendingAnimeUseCase: GetTrendingAnimeUseCase by lazy {
        GetTrendingAnimeUseCase(
            repository = discoverRepository
        )
    }
    
    /**
     * 获取本季新番的 UseCase
     */
    private val getCurrentSeasonAnimeUseCase: GetCurrentSeasonAnimeUseCase by lazy {
        GetCurrentSeasonAnimeUseCase(
            repository = discoverRepository
        )
    }
    
    /**
     * 获取排行榜动漫的 UseCase
     */
    private val getTopRankedAnimeUseCase: GetTopRankedAnimeUseCase by lazy {
        GetTopRankedAnimeUseCase(
            repository = discoverRepository
        )
    }
    
    /**
     * 获取动漫详情的 UseCase
     */
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase by lazy {
        JikanAnimeDetailRepository(
            jikanApi = jikanApi,
            mapper = jikanToAnimeDetailMapper
        )
    }
    
    /**
     * 获取相关动漫的 UseCase
     */
    private val getRelatedAnimeUseCase: GetRelatedAnimeUseCase by lazy {
        JikanRelatedAnimeRepository(
            jikanApi = jikanApi,
            mapper = jikanToAnimeDetailMapper
        )
    }
    
    /**
     * 获取推荐动漫的 UseCase
     */
    private val getAnimeRecommendationsUseCase: GetAnimeRecommendationsUseCase by lazy {
        JikanAnimeRecommendationsRepository(
            jikanApi = jikanApi,
            mapper = jikanToAnimeDetailMapper
        )
    }
    
    /**
     * 搜索动漫的 UseCase
     */
    private val searchAnimeUseCase: SearchAnimeUseCase by lazy {
        SearchAnimeUseCase(
            repository = discoverRepository
        )
    }
    
    // ========================================
    // Feature 层：ViewModel
    // ========================================
    
    /**
     * 创建 DiscoverViewModel
     * 
     * 使用 androidx.lifecycle.ViewModel，自动管理生命周期
     */
    fun createDiscoverViewModel(): DiscoverViewModel {
        return DiscoverViewModel(
            getTrendingAnimeUseCase = getTrendingAnimeUseCase,
            getCurrentSeasonAnimeUseCase = getCurrentSeasonAnimeUseCase,
            getTopRankedAnimeUseCase = getTopRankedAnimeUseCase
        )
    }
    
    /**
     * 创建 AnimeDetailViewModel
     * 
     * 使用 androidx.lifecycle.ViewModel，自动管理生命周期
     */
    fun createAnimeDetailViewModel(): AnimeDetailViewModel {
        return AnimeDetailViewModel(
            getAnimeDetailUseCase = getAnimeDetailUseCase,
            getRelatedAnimeUseCase = getRelatedAnimeUseCase,
            getAnimeRecommendationsUseCase = getAnimeRecommendationsUseCase
        )
    }
    
    /**
     * 创建 SearchViewModel
     *
     * 使用 androidx.lifecycle.ViewModel，自动管理生命周期
     */
    fun createSearchViewModel(): SearchViewModel {
        return SearchViewModel(
            searchAnimeUseCase = searchAnimeUseCase
        )
    }

    /**
     * 创建 AnimeListViewModel
     *
     * @param listType 列表类型（热门、本季新番、排行榜）
     * 
     * 使用 androidx.lifecycle.ViewModel，自动管理生命周期
     */
    fun createAnimeListViewModel(listType: AnimeListType): AnimeListViewModel {
        return AnimeListViewModel(
            listType = listType,
            getTrendingAnimeUseCase = getTrendingAnimeUseCase,
            getCurrentSeasonAnimeUseCase = getCurrentSeasonAnimeUseCase,
            getTopRankedAnimeUseCase = getTopRankedAnimeUseCase
        )
    }
}

