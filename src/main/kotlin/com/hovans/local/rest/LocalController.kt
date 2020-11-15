package com.hovans.local.rest

import com.hovans.local.domain.local.LocalRepository
import com.hovans.local.domain.local.LocalService
import com.hovans.local.domain.local.Ranking
import com.hovans.local.domain.local.RankingRepository
import com.hovans.local.domain.local.repository.KakaoRepository
import com.hovans.local.domain.local.repository.NaverRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*


@RestController
class LocalController {

	final var localService: LocalService

	init {
		val repositories = ArrayList<LocalRepository>()
		repositories.add(KakaoRepository())
		repositories.add(NaverRepository())
		localService = LocalService(repositories)
	}

	@GetMapping(value = ["/v1/places"])
	fun getPlaces(@RequestParam(name = "keyword", defaultValue = "") keyword: String,
	              @RequestParam(name="cursor", required = false) cursor: String?): ResponseEntity<SearchRes> {
		if (keyword == "") {
			return ResponseEntity(HttpStatus.BAD_REQUEST)
		}
		val (places, nextCursor) = localService.getPlaces(keyword = keyword, cursor)
		return ResponseEntity<SearchRes>(SearchRes(places, nextCursor), HttpStatus.OK)
	}

	@GetMapping(value = ["/v1/rankings"])
	fun getRankings(): ResponseEntity<RankingRes> {
		return ResponseEntity(RankingRes(localService.getRanking()), HttpStatus.OK)
	}

	@GetMapping(value = ["/"])
	fun home(): ResponseEntity<String> {
		val htmlContent = ("<!DOCTYPE html><html><body>"
				+ "<h1>Welcome to the Spring DDD REST Webservice.</h1>"
				+ "</body></html>")
		return ResponseEntity(htmlContent, HttpStatus.OK)
	}
}