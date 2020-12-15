package com.hovans.local.domain.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.*

@SpringBootTest
class DbRankingRepositoryTest @Autowired constructor(private val jpaRepository: JpaRankingRepository) {

	val rankingRepository = DbRankingRepository(jpaRepository)

	@BeforeEach
	fun cleanUp() {
		jpaRepository.deleteAll()
	}

	@Test
	fun increase() {
		val keyword = "test keyword"
		val count = Random().nextInt(10) + 1

		for (i in 1..count) {
			rankingRepository.increase(keyword)
		}
		val ranking = jpaRepository.getOne(keyword)
		assertThat(ranking.count).isEqualTo(count)
	}

	@ParameterizedTest
	@ValueSource(ints = [5, 10, 15])
	fun getRankings(count: Int) {
		val keywords = HashMap<String, Int>()
		for (i in 1..count) {
			keywords[i.toString()] = i
			for (j in 1..i) rankingRepository.increase(i.toString())
		}

		val rankings = rankingRepository.getRankings()

		var expectedSize = count
		if (expectedSize > 10) expectedSize = 10

		assertThat(rankings.size).isEqualTo(expectedSize)

		for (i in 0 until expectedSize) {
			assertThat(rankings[i].keyword).isEqualTo((count - i).toString())
			assertThat(rankings[i].count).isEqualTo(keywords[(count - i).toString()])
		}
	}

	@Test
	fun getRankings_changeOrder() {
		// Given
		val prior = "prior"
		val successor = "successor"
		for (i in 1..4) rankingRepository.increase(successor)
		for (i in 1..5) rankingRepository.increase(prior)
		var rankings = rankingRepository.getRankings()
		assertThat(rankings[0].keyword).isEqualTo(prior)
		assertThat(rankings[1].keyword).isEqualTo(successor)
		for (i in 1..2) rankingRepository.increase(successor)

		// When
		rankings = rankingRepository.getRankings()

		// Then
		assertThat(rankings[0].keyword).isEqualTo(successor)
		assertThat(rankings[0].count).isEqualTo(6)
		assertThat(rankings[1].keyword).isEqualTo(prior)
		assertThat(rankings[1].count).isEqualTo(5)
	}
}