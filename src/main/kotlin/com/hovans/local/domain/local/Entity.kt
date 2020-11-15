package com.hovans.local.domain.local


class Place(
		val title: String,
		val imageUrls: List<String>,
)

class Cursor(
		val totalPages: Int,
		val currentPage: Int,
		val pageSize: Int,
)

class Ranking(
		val keyword: String,
		val count: Int,
)