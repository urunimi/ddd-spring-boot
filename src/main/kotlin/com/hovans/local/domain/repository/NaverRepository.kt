package com.hovans.local.domain.repository

import com.hovans.local.domain.Cursor
import com.hovans.local.domain.LocalRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit


class NaverRepository : LocalRepository {

	private val defaultSize = 3
	private val naverClient: NaverClient

	init {
		val secretKey = System.getenv("NAVER_SECRET_KEY") ?: throw RuntimeException("{{NAVER_SECRET_KEY}} env variable is not set.")

		val httpClient = OkHttpClient()
				.newBuilder()
				.readTimeout(10, TimeUnit.SECONDS)
				.connectTimeout(4, TimeUnit.SECONDS)
				.addInterceptor {
					val request: Request = it.request()
							.newBuilder()
							.addHeader("X-Naver-Client-Id", "QvHsrcraJbAXM4Qh0x5t")
							.addHeader("X-Naver-Client-Secret", secretKey)
							.build()
					it.proceed(request)
				}.build()
		val retrofit = Retrofit.Builder().baseUrl("https://openapi.naver.com")
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClient)
				.build()
		naverClient = retrofit.create(NaverClient::class.java)
	}

	override fun getPlaceNames(keyword: String, cursor: Cursor): Pair<List<String>, Cursor> {
		val res = naverClient.getPlaces(keyword, defaultSize).get()
		val placeNames = ArrayList<String>()
		for (document in res.items) {
			placeNames.add(document.title)
		}

		return Pair(placeNames, Cursor(
				totalPages = 1, // Naver 의 경우 검색 시작 위치가 1이 최대라서 Pagination 이 불가능
				currentPage = cursor.currentPage + 1,
				pageSize = defaultSize,
		))
	}

	override fun getPlaceImages(placeName: String): List<String> {
		val res = naverClient.getImages(placeName, defaultSize).get()
		val imageUrls = ArrayList<String>()
		for (doc in res.items) {
			imageUrls.add(doc.link)
		}
		return imageUrls
	}
}


interface NaverClient {
	@GET("/v1/search/local.json")
	fun getPlaces(
			@Query("query") query: String,
			@Query("display") size: Int,
	): CompletableFuture<NaverLocalResponse>

	@GET("/v1/search/image.json")
	fun getImages(
			@Query("query") query: String,
			@Query("display") size: Int,
	): CompletableFuture<NaverImageResponse>
}

class NaverLocalResponse(val items: List<NaverPlace>)
class NaverPlace(val title: String)

class NaverImageResponse(val items: List<NaverImage>)
class NaverImage(val link: String)