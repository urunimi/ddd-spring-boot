package com.hovans.local.domain.local

interface LocalRepository {
	fun getPlaceNames(keyword: String, cursor: Cursor): Pair<List<String>, Cursor>
	fun getPlaceImages(placeName: String): List<String>
}