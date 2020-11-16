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
	private val retryCount = 10

	/**
	 * rankings
	 * Top 10 을 캐시에 저장해서 Worst 케이스 제외 O(1) 시간 안에 Ranking 을 가져올 수 있도록 한다.
	 */
	private var rankings = Collections.synchronizedList(ArrayList<Ranking>())
	private var rankingSet = Collections.synchronizedSet(HashSet<String>())

	override fun increase(keyword: String): Int {
		for (i in 1..retryCount) { // Optimistic locking
			try {
				val rankingHolder = jpaRepository.findById(keyword)
				var dbRanking = DbRanking(keyword = keyword, count = 1, version = 1)
				if (rankingHolder.isPresent) {
					dbRanking = rankingHolder.get()
					dbRanking.count += 1
				}
				jpaRepository.save(dbRanking)
				resetCacheIfItNeeds(Ranking(keyword = keyword, count = dbRanking.count))
				return dbRanking.count
			} catch (ex: Exception) {
				logger.error("increase", ex)
				if (i == retryCount) throw ex   // Retry 를 모두 실패한 경우 Server error 발생
			}
		}
		return 0
	}

	private fun resetCacheIfItNeeds(ranking: Ranking) {
		if (rankings.isEmpty()) return
		if (rankingSet.contains(ranking.keyword) || rankings[rankings.size-1].count < ranking.count) {
			rankingSet.clear()
			rankings.clear()
		}
	}

	@Synchronized
	private fun buildRankingsFromDb() {
		val dbRankings = jpaRepository.findTop10ByOrderByCountDesc()
		val rankings = ArrayList<Ranking>()
		for (dbRanking in dbRankings) {
			val ranking = Ranking(keyword = dbRanking.keyword, count = dbRanking.count)
			rankings.add(ranking)
			rankingSet.add(ranking.keyword)
		}
		this.rankings = rankings
	}

	override fun getRankings(): List<Ranking> {
		if (rankings.isEmpty()) {
			buildRankingsFromDb()
		}
		return rankings
	}
}

@Entity(name = "ranking")
@Table(indexes = [Index(name = "IDX_COUNT", columnList = "count")])
class DbRanking(
		@Id val keyword: String,
		var count: Int,
		@Suppress("unused")
		@Version var version: Int, // Optimistic locking 을 지원하기 위해
)

interface JpaRankingRepository : JpaRepository<DbRanking, String> {
	fun findTop10ByOrderByCountDesc(): List<DbRanking>
}