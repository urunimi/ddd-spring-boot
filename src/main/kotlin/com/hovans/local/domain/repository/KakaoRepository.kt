package com.hovans.local.domain.repository

import com.google.gson.annotations.SerializedName
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


class KakaoRepository : LocalRepository {

	private val defaultSize = 3
	private val kakaoClient: KakaoClient

	init {
		val apiKey = System.getenv("KAKAO_API_KEY")?: throw RuntimeException("{{KAKAO_API_KEY}} env variable is not set.")

		val httpClient = OkHttpClient()
				.newBuilder()
				.readTimeout(10, TimeUnit.SECONDS)
				.connectTimeout(4, TimeUnit.SECONDS)
				.addInterceptor {
					val request: Request = it.request()
							.newBuilder()
							.addHeader("Authorization", apiKey)
							.build()
					it.proceed(request)
				}.build()
		val retrofit = Retrofit.Builder().baseUrl("https://dapi.kakao.com")
				.addConverterFactory(GsonConverterFactory.create())
				.client(httpClient)
				.build()
		kakaoClient = retrofit.create(KakaoClient::class.java)
	}

	override fun getPlaceNames(keyword: String, cursor: Cursor): Pair<List<String>, Cursor> {
		val res = kakaoClient.getPlaces(keyword, cursor.currentPage, defaultSize).get()
		val placeNames = ArrayList<String>()
		for (document in res.documents) {
			placeNames.add(document.placeName)
		}

		var totalPages = res.meta.totalCount / defaultSize
		if (res.meta.totalCount % defaultSize != 0) {
			totalPages += 1
		}

		val nextCursor = Cursor(
				totalPages = totalPages,
				currentPage = cursor.currentPage + 1,
				pageSize = defaultSize,
		)
		return Pair(placeNames, nextCursor)
	}

	override fun getPlaceImages(placeName: String): List<String> {
		val res = kakaoClient.getImages(placeName, defaultSize).get()
		val imageUrls = ArrayList<String>()
		for (doc in res.documents) {
			imageUrls.add(doc.imageUrl)
		}
		return imageUrls
	}
}


interface KakaoClient {
	@GET("/v2/local/search/keyword.json")
	fun getPlaces(
			@Query("query") query: String,
			@Query("page") page: Int,
			@Query("size") size: Int
	): CompletableFuture<KakaoLocalResponse>

	@GET("/v2/search/image")
	fun getImages(
			@Query("query") query: String,
			@Query("size") size: Int
	): CompletableFuture<KakaoImageResponse>
}

class KakaoLocalResponse(val documents: List<KakaoPlace>, val meta: KakaoMetaRes)
class KakaoPlace(@SerializedName("place_name") val placeName: String)
class KakaoMetaRes(@SerializedName("total_count") val totalCount: Int)

class KakaoImageResponse(val documents: List<KakaoImage>)
class KakaoImage(@SerializedName("image_url") val imageUrl: String)
