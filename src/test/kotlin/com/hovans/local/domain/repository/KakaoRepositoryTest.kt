package com.hovans.local.domain.repository

import com.hovans.local.domain.Cursor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * KakaoRepositoryTest
 *
 * 실제 외부 API 서비스에 리퀘스트를 보내지 못하도록 응답을 모킹하도록 구현
 */
class KakaoRepositoryTest {

	lateinit var mockWebServer: MockWebServer
	lateinit var kakaoRepository: KakaoRepository

	@BeforeEach
	fun beforeEach() {
		mockWebServer = MockWebServer()
		mockWebServer.start()
		kakaoRepository = KakaoRepository(mockWebServer.url(""))
	}

	@AfterEach
	fun afterEach() {
		mockWebServer.shutdown()
	}

	@Test
	fun getPlaceNames() {
		// Given
		val keyword = "keyword"
		val cursor = Cursor(
				totalPages = 1,
				currentPage = 1,
				pageSize = 3,
		)
		val placeNames = listOf("One", "Two", "Three")
		mockWebServer.enqueue(MockResponse()
				.addHeader("Content-Type", "application/json; charset=utf-8")
				.setBody("{" +
						"  \"documents\": [" +
						"    {" +
						"      \"place_name\": \"${placeNames[0]}\"" +
						"    }," +
						"    {" +
						"      \"place_name\": \"${placeNames[1]}\"" +
						"    }," +
						"    {" +
						"      \"place_name\": \"${placeNames[2]}\"" +
						"    }" +
						"  ]," +
						"  \"meta\": {" +
						"    \"is_end\": false," +
						"    \"pageable_count\": 32," +
						"    \"same_name\": {" +
						"      \"keyword\": \"${keyword}\"," +
						"      \"region\": []," +
						"      \"selected_region\": \"\"" +
						"    }," +
						"    \"total_count\": 32" +
						"  }" +
						"}"))

		// When
		val (res, nextCursor) = kakaoRepository.getPlaceNames(keyword, cursor)

		// Then
		for (i in 0..res.size - 1) {
			assertThat(res[i]).isEqualTo(placeNames[i])
		}
		assertThat(nextCursor.currentPage).isEqualTo(cursor.currentPage + 1)
	}


	@Test
	fun getPlaceImages() {
		val placeName = "place"
		val imageUrls = listOf("http://1", "http://2", "http://3")
		mockWebServer.enqueue(MockResponse()
				.addHeader("Content-Type", "application/json; charset=utf-8")
				.setBody("{" +
						"  \"documents\": [" +
						"    {" +
						"      \"image_url\": \"${imageUrls[0]}\"" +
						"    }," +
						"    {" +
						"      \"image_url\": \"${imageUrls[1]}\"" +
						"    }," +
						"    {" +
						"      \"image_url\": \"${imageUrls[2]}\"" +
						"    }" +
						"  ]," +
						"  \"meta\": {" +
						"    \"is_end\": false," +
						"    \"pageable_count\": 32," +
						"    \"total_count\": 32" +
						"  }" +
						"}"))

		// When
		val res = kakaoRepository.getPlaceImages(placeName)

		// Then
		for (i in 0..res.size - 1) {
			assertThat(res[i]).isEqualTo(imageUrls[i])
		}
	}
}