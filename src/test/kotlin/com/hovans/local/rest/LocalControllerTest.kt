package com.hovans.local.rest

import com.hovans.local.domain.LocalService
import com.hovans.local.domain.Place
import com.hovans.local.domain.Ranking
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@SpringBootTest
@ExtendWith(MockitoExtension::class)
class LocalControllerTest constructor(
	@Mock private val localService: LocalService,
) {
		private val localController: LocalController = LocalController(localService)

	@Test
	fun getPlaces_OK() {
		// Given
		val keyword = "keyword"
		val place = Place(title = "title", imageUrls = listOf("http://imagehub.com/images?id=1"))
		val req = MockHttpServletRequest()
		RequestContextHolder.setRequestAttributes(ServletRequestAttributes(req))
		`when`(localService.getPlaces(eq(keyword), anyOrNull()))
				.thenReturn(Pair(listOf(place), null))
		// When
		val res = localController.getPlaces(keyword, null)
		// Then
		assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(res.body!!.places[0]).isEqualTo(place)
	}

	@Test
	fun getPlaces_BadRequest() {
		// Given
		val req = MockHttpServletRequest()
		RequestContextHolder.setRequestAttributes(ServletRequestAttributes(req))
		// When
		val res = localController.getPlaces("", null)
		// Then
		assertThat(res.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
	}

	@Test
	fun getRankings_OK() {
		// Given
		val req = MockHttpServletRequest()
		RequestContextHolder.setRequestAttributes(ServletRequestAttributes(req))
		val ranking = Ranking("top", 1)
		`when`(localService.getRankings())
				.thenReturn(listOf(ranking))
		// When
		val res = localController.getRankings()
		// Then
		assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(res.body!!.rankings[0]).isEqualTo(ranking)
	}

	@Test
	fun getHome_OK() {
		// Given
		val req = MockHttpServletRequest()
		RequestContextHolder.setRequestAttributes(ServletRequestAttributes(req))
		// When
		val res = localController.home()
		// Then
		assertThat(res.statusCode).isEqualTo(HttpStatus.OK)
	}
}