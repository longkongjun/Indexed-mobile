package com.pusu.indexed.domain.anime.usecase

import com.pusu.indexed.domain.anime.model.AnimeItem
import com.pusu.indexed.domain.anime.model.AnimeDetailData

/**
 * 获取动漫详情的用例
 */
interface GetAnimeDetailUseCase {
    suspend operator fun invoke(animeId: Int): Result<AnimeDetailData>
}

/**
 * 获取相关动漫的用例
 */
interface GetRelatedAnimeUseCase {
    suspend operator fun invoke(animeId: Int): Result<List<AnimeItem>>
}

/**
 * 获取推荐动漫的用例
 */
interface GetAnimeRecommendationsUseCase {
    suspend operator fun invoke(animeId: Int): Result<List<AnimeItem>>
}

