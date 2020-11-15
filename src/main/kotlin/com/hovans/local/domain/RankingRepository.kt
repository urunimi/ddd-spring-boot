package com.hovans.local.domain

interface RankingRepository {
	fun increase(keyword: String): Int
	fun getRankings(): List<Ranking>
}