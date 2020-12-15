package com.hovans.local.domain

import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class LocalService constructor(val localRepositories: List<LocalRepository>, val rankingRepository: RankingRepository) {

	private val logger = LoggerFactory.getLogger(LocalService::class.java)
	val gson = Gson()

	fun getPlaces(keyword: String, prevCursor: String?): Pair<List<Place>, String?> {
		val (placeNames, cursorStr) = getPlaceNames(keyword, prevCursor)
		val places = ArrayList<Place>()
		for (placeName in placeNames) {
			places.add(Place(placeName, getPlaceImageUrls(placeName)))
		}
		rankingRepository.increase(keyword)
		return Pair(places, cursorStr)
	}

	private fun getPlaceImageUrls(placeName: String): List<String> {
		for (repository in localRepositories) {
			try {
				val imageUrls = repository.getPlaceImages(placeName)
				if (imageUrls.isNotEmpty()) {
					return imageUrls
				}
			} catch (ex: Exception) {
				logger.error("getPlaceImageUrls", ex)
			}
		}
		return ArrayList<String>()
	}

	/**
	 * 주어진 Repository 를 돌면서 placeName 을 가져오고 정상 응답을 가져온 경우 리턴
	 * 아무런 응답을 받지 못한 경우에도 빈 응답을 리턴
	 */
	private fun getPlaceNames(keyword: String, cursorStr: String?): Pair<List<String>, String?> {
		var prevCursor = Cursor(
				totalPages = 0,
				currentPage = 1,
				pageSize = 0,
		)
		cursorStr?.let {
			prevCursor = gson.fromJson(cursorStr, Cursor::class.java)
		}

		for (repository in localRepositories) {
			try {
				val (placeNames, cursor) = repository.getPlaceNames(keyword, prevCursor)
				if (cursor.currentPage > cursor.totalPages) {  // Last page
					return Pair(placeNames, null)
				}
				return Pair(placeNames, gson.toJson(cursor))
			} catch (ex: Exception) {
				logger.error("getPlaceNames", ex)
			}
		}
		throw RuntimeException("Failed to get placeNames")
	}

	fun getRankings(): List<Ranking> {
		return rankingRepository.getRankings()
	}
}