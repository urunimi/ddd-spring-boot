package com.hovans.local.domain.local

interface RankingRepository {
	fun increase(keyword: String): Int
	fun getRankings(): List<Ranking>
}