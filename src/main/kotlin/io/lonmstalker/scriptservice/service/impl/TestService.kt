package io.lonmstalker.scriptservice.service.impl

import io.lonmstalker.scriptservice.model.DomainObject
import io.lonmstalker.scriptservice.model.TestResult
import io.lonmstalker.scriptservice.service.ScriptService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.graalvm.polyglot.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.function.Tuples
import java.util.*
import java.util.function.Supplier

// https://github.com/oracle/graaljs/tree/master/graal-js/src/com.oracle.truffle.js.test.threading/src/com/oracle/truffle/js/test/threading
@Service
class TestService(
    private val scriptService: ScriptService
) {

    suspend fun startTest(scriptId: UUID): TestResult {
        println("in runtime")
        val testResult = TestResult()
        var startTime = System.nanoTime()

        initTestTask(
            Mono.fromCallable {
                this.scriptService.executeRuntimeCode(scriptId, DomainObject(UUID.randomUUID()))
            }
        )

        testResult.runtimeCodeTime = System.nanoTime() - startTime
        println("in compile")
        startTime = System.nanoTime()

        initTestTask(
            Mono.fromCallable {
                this.scriptService.executeCompiledCode(scriptId, DomainObject(UUID.randomUUID()))
            }
        )

        testResult.compiledCodeTime = System.nanoTime() - startTime
        println("in ctx")
        startTime = System.nanoTime()

        initTestTask(
            Mono.fromCallable {
                this.scriptService.executeWithNewContext(scriptId, DomainObject(UUID.randomUUID()))
            }
        )

        testResult.newContextRuntimeCodeTime = System.nanoTime() - startTime
        println("in thread local compiled without lock")
        startTime = System.nanoTime()

        initTestTask(
            Mono.fromCallable {
                this.scriptService.executeThreadLocalCompiledCode(scriptId, DomainObject(UUID.randomUUID()))
            }
        )

        testResult.threadLocalCompiledCodeTime = System.nanoTime() - startTime

        return testResult
    }

    private suspend fun initTestTask(monoTask: Mono<Value>) = Flux.range(1, 10)
            .parallel().runOn(Schedulers.boundedElastic())
            .flatMap { monoTask }
            .sequential()
            .collectList()
            .awaitFirstOrNull()

}