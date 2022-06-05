package io.lonmstalker.scriptservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ScriptServiceApplication

fun main(args: Array<String>) {
	runApplication<ScriptServiceApplication>(*args)
}
