package com.hovans.local.domain.repository

import com.hovans.local.domain.Ranking
import com.hovans.local.domain.RankingRepository
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table

class DbRankingRepository(private val jpaRepository: JpaRankingRepository) : RankingRepository {

	private var rankingCache: List<Ranking>? = null

	override fun increase(keyword: String): Int {
		val rankingHolder = jpaRepository.findById(keyword)
		val ranking = DbRanking(keyword = keyword, count = 1)
		if (rankingHolder.isPresent) {
			ranking.count = rankingHolder.get().count + 1
		}
		jpaRepository.save(ranking)
		invalidateCacheIfItNeeds(ranking.count)
		return ranking.count
	}

	private fun invalidateCacheIfItNeeds(newCount: Int) {
		rankingCache?.let { it ->
			if (it[it.size-1].count < newCount) {
				rankingCache = null
			}
		}
	}

	private fun buildCache() {
		val dbRankings = jpaRepository.findTop10ByOrderByCountDesc()
		val rankings = ArrayList<Ranking>()
		for (dbRanking in dbRankings) {
			rankings.add(Ranking(keyword = dbRanking.keyword, count = dbRanking.count))
		}
		rankingCache = rankings
	}

	override fun getRankings(): List<Ranking> {
		if (rankingCache == null) {
			buildCache()
		}
		return rankingCache!!
	}
}

@Entity(name = "ranking")
@Table(indexes = [Index(name = "IDX_COUNT", columnList = "count")])
class DbRanking(
		@Id val keyword: String,
		var count: Int,
)

interface JpaRankingRepository : JpaRepository<DbRanking, String> {
	fun findTop10ByOrderByCountDesc(): List<DbRanking>
}