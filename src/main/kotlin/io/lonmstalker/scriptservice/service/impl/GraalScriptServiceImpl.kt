package io.lonmstalker.scriptservice.service.impl

import io.lonmstalker.scriptservice.model.DomainObject
import io.lonmstalker.scriptservice.model.Script
import io.lonmstalker.scriptservice.repository.ScriptRepository
import io.lonmstalker.scriptservice.service.ScriptService
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.graalvm.polyglot.*
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.PostConstruct

@Service
@DependsOn("liquibase")
class GraalScriptServiceImpl(
    private val scriptRepository: ScriptRepository
) : ScriptService {
    private val scriptMap = mutableMapOf<UUID, Script>()
    private val scriptCompiledMap: MutableMap<UUID, Value> = mutableMapOf()
    private val scriptThreadLocalMap: MutableMap<UUID, ThreadLocal<Value>> = mutableMapOf()
    private val threadLocalCtx = ThreadLocal.withInitial{ getCtx() }

    @PostConstruct
    fun init() {
        runBlocking {
            scriptRepository.findAll()
                .toList().forEach {
                    scriptMap[it.id] = it
                    scriptCompiledMap[it.id] = ctx.eval(
                        Source.newBuilder(
                            "js", it.value, it.title + ".js"
                        ).build()
                    )
                    scriptThreadLocalMap[it.id] = ThreadLocal.withInitial {
                        threadLocalCtx.get().eval(
                            Source.newBuilder(
                                "js", it.value, it.title + ".js"
                            ).build()
                        )
                    }
                }
        }
    }

    override fun executeRuntimeCode(id: UUID, data: DomainObject): Value =
        try {
            ctxLock.lock()
            ctx.enter()
            val result = ctx.eval("js", this.scriptMap[id]!!.value)
                .execute(data)
            ctx.leave()
            result
        } finally {
            ctxLock.unlock()
        }

    override fun executeCompiledCode(id: UUID, data: DomainObject): Value =
        try {
            ctxLock.lock()
            ctx.enter()
            val result = this.scriptCompiledMap[id]!!.execute(data)
            ctx.leave()
            result
        } finally {
            ctxLock.unlock()
        }

    override fun executeWithNewContext(id: UUID, data: DomainObject): Value =
        getCtx().eval("js", this.scriptMap[id]!!.value)
            .execute(data)

    override fun executeThreadLocalCompiledCode(id: UUID, data: DomainObject): Value =
        this.scriptThreadLocalMap[id]!!.get().execute(data)

    companion object {
        @JvmStatic
        private val ctxLock = ReentrantLock()

        @JvmStatic
        private val ctx: Context = getCtx()

        @JvmStatic
        private fun getCtx() = Context.newBuilder()
            .allowAllAccess(true)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(HostAccess.ALL)
            .option("engine.WarnInterpreterOnly", "false") // if no graalvm compiler
            .allowHostClassLoading(true)
            .allowHostClassLookup { true }
            .build()
    }
}