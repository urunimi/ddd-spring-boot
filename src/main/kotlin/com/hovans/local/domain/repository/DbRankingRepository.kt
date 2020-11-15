package com.hovans.local.domain.repository

import com.hovans.local.domain.Ranking
import com.hovans.local.domain.RankingRepository
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*
import javax.persistence.*


/**
 * DbRankingRepository
 *
 * RankingRepository 를 구현한 구현체.
 * rankingCache 를 통해 Worst 케이스 제외 O(1) 시간 안에 Ranking 을 가져올 수 있도록 한다.
 *
 * @param jpaRepository: JpaRankingRepository 의 구현체 (RestController 에서 주입한다.
 */
class DbRankingRepository(private val jpaRepository: JpaRankingRepository) : RankingRepository {

	private val logger = LoggerFactory.getLogger(DbRankingRepository::class.java)

	/**
	 * rankingCache
	 * Top 10 을 캐시에 저장해서 Worst 케이스 제외 O(1) 시간 안에 Ranking 을 가져올 수 있도록 한다.
	 */
	private var rankingCache: List<Ranking>? = null

	override fun increase(keyword: String): Int {
		for (i in 0..10) { // Optimistic locking
			try {
				val rankingHolder = jpaRepository.findById(keyword)
				var ranking = DbRanking(keyword = keyword, count = 1, version = 1)
				if (rankingHolder.isPresent) {
					ranking = rankingHolder.get()
					ranking.count += 1
				}
				jpaRepository.save(ranking)
				invalidateCacheIfItNeeds(ranking.count)
				return ranking.count
			} catch (ex: Exception) {
				logger.error("increase", ex)
			}
		}
		return 0
	}

	/**
	 * 어떤 키워드의 호출 count 가 증가했을 때 아래 두가지 경우 cache 를 invalidate 한다.
	 * - 캐시의 사이즈가 10이 안될 때
	 * - 캐시의 마지막 아이템의 count 보다 새로운 count 가 클 때
	 */
	private fun invalidateCacheIfItNeeds(newCount: Int) {
		rankingCache?.let { it ->
			if (it.size < 10 || it[it.size - 1].count < newCount) {
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
		@Version var version: Int, // Optimistic locking 을 지원하기 위해
)

interface JpaRankingRepository : JpaRepository<DbRanking, String> {
	fun findTop10ByOrderByCountDesc(): List<DbRanking>
}