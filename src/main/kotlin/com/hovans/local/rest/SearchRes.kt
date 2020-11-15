package com.hovans.local.rest

import com.hovans.local.domain.local.Place
import com.hovans.local.domain.local.Ranking

class SearchRes(
		val places: List<Place>,
		val cursor: String?,
)

class RankingRes(
		val rankings: List<Ranking>,
)