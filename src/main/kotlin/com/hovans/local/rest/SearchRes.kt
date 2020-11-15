package com.hovans.local.rest

import com.hovans.local.domain.local.Place

class SearchRes(
		val places: List<Place>,
		val cursor: String?,
)