package com.hovans.local.rest

import com.hovans.local.domain.LocalService
import com.hovans.local.domain.repository.DbRankingRepository
import com.hovans.local.domain.repository.JpaRankingRepository
import com.hovans.local.domain.repository.KakaoRepository
import com.hovans.local.domain.repository.NaverRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
class LocalController {

	final var localService: LocalService

	@Autowired constructor(jpaRankingRepository: JpaRankingRepository) {
		val repositories = listOf(KakaoRepository(), NaverRepository())
		localService = LocalService(repositories, DbRankingRepository(jpaRankingRepository))
	}

	constructor(localService: LocalService) {
		this.localService = localService
	}

	@GetMapping(value = ["/v1/places"])
	fun getPlaces(@RequestParam(name = "keyword") keyword: String,
	              @RequestParam(name="cursor", required = false) cursor: String?): ResponseEntity<SearchRes> {
		if (keyword == "") {
			return ResponseEntity(HttpStatus.BAD_REQUEST)
		}
		val (places, nextCursor) = localService.getPlaces(keyword = keyword, cursor)
		return ResponseEntity<SearchRes>(SearchRes(places, nextCursor), HttpStatus.OK)
	}

	@GetMapping(value = ["/v1/rankings"])
	fun getRankings(): ResponseEntity<RankingRes> {
		return ResponseEntity(RankingRes(localService.getRankings()), HttpStatus.OK)
	}

	@GetMapping(value = ["/"])
	fun home(): ResponseEntity<String> {
		val htmlContent = ("<!DOCTYPE html><html><body>"
				+ "<h1>Welcome to the Spring DDD REST Webservice.</h1>"
				+ "</body></html>")
		return ResponseEntity(htmlContent, HttpStatus.OK)
	}
}