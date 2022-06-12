package io.lonmstalker.scriptservice.controller

import io.lonmstalker.scriptservice.repository.ScriptRepository
import io.lonmstalker.scriptservice.service.impl.TestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TestController(
    private val testService: TestService,
    private val scriptRepository: ScriptRepository
) {

    @GetMapping
    suspend fun test(@RequestParam id: UUID) = this.testService.startTest(id)

    @GetMapping("/list")
    suspend fun getAll() = this.scriptRepository.findAll()
}