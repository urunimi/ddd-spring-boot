package com.hovans.local

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.hovans.local.rest"])
class LocalSearchApplication

fun main(args: Array<String>) {
	runApplication<LocalSearchApplication>(*args)
}
