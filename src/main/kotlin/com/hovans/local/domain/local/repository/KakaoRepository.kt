package com.hovans.local.domain.local.repository

import com.hovans.local.domain.local.LocalRepository
import com.hovans.local.domain.local.Meta

class KakaoRepository : LocalRepository {
	override fun getPlaceNames(keyword: String, count: Int): Pair<List<String>, Meta> {
		TODO("Not yet implemented")
	}

	override fun getPlaceImages(placeName: String, count: Int): List<String> {
		TODO("Not yet implemented")
	}
}