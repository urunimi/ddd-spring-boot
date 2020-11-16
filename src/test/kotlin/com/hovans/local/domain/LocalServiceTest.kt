package com.hovans.local.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
class LocalServiceTest constructor(
		@Mock val localRepository: LocalRepository,
		@Mock val rankingRepository: RankingRepository,
) {
	private val localService: LocalService

	init {
		val localRepositories = ArrayList<LocalRepository>()
		localRepositories.add(localRepository)
		localService = LocalService(localRepositories, rankingRepository)
	}

	@Test
	fun getPlaces_Found() {
		// Given
		val keyword = "keyword"
		val placeNames = listOf("one", "two")
		val imageUrl = "http://imagehub.com/images?id=1"
		`when`(localRepository.getPlaceNames(eq(keyword), any()))
				.thenReturn(Pair(placeNames, Cursor(2, 2, 3)))
		`when`(localRepository.getPlaceImages(any()))
				.thenReturn(listOf(imageUrl))

		// When
		val (places, cursor) = localService.getPlaces(keyword, "{\"totalPages\":2,\"currentPage\":1,\"pageSize\":3}")

		// Then
		for (i in 0..places.size - 1) {
			assertThat(places[i].title).isEqualTo(placeNames[i])
			assertThat(places[i].imageUrls[0]).isEqualTo(imageUrl)
		}
		assertThat(cursor).isEqualTo("{\"totalPages\":2,\"currentPage\":2,\"pageSize\":3}")
		verify(rankingRepository, times(1)).increase(eq(keyword))
	}

	@Test
	fun getPlaces_NotFound() {
		val keyword = "keyword"
		`when`(localRepository.getPlaceNames(any(), any()))
				.thenReturn(Pair(ArrayList<String>(), Cursor(1, 2, 3)))

		val (places, cursor) = localService.getPlaces(keyword, null)

		assertThat(cursor).isNull()
		assertThat(places).isEmpty()
		verify(rankingRepository, times(1)).increase(eq(keyword))
	}

	@Test
	fun getRankings() {
		val rankings = listOf(Ranking("two", 2), Ranking("one", 1))
		`when`(rankingRepository.getRankings())
				.thenReturn(rankings)

		val res = localService.getRankings()

		for (i in 0..rankings.size - 1) {
			assertThat(rankings[i]).isEqualTo(res[i])
		}
	}
}