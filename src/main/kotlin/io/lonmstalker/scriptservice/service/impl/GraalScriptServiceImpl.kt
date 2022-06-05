package io.lonmstalker.scriptservice.service.impl

import com.oracle.truffle.polyglot.PolyglotImpl
import io.lonmstalker.scriptservice.model.DomainObject
import io.lonmstalker.scriptservice.service.ScriptService
import org.graalvm.polyglot.*
import org.springframework.stereotype.Service
import java.util.UUID
import javax.annotation.PostConstruct

@Service
class ScriptServiceImpl : ScriptService {

    @PostConstruct
    fun init() {
        executeScripts()
    }

    fun executeScripts() {
        val engine = Engine.newBuilder()
            .allowExperimentalOptions(true)
            .build()
        val ctx = Context.newBuilder()
            .engine(engine)
            .allowAllAccess(true)
            .allowExperimentalOptions(true)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(HostAccess.ALL)
            .allowHostClassLoading(true)
            .allowHostClassLookup { true }
            .build()
        val src = createSource()

        var start = System.currentTimeMillis()
        for (i in 1..10) {
            ctx.eval(src)
                .execute(DomainObject(UUID.randomUUID()))
        }
        println("end first: ${System.currentTimeMillis() - start}")

        start = System.currentTimeMillis()
        val eval = ctx.eval(src)
        for (i in 1..10) {
            eval.execute(DomainObject(UUID.randomUUID()))
        }
        println("end second: ${System.currentTimeMillis() - start}")
    }

    companion object {
        @JvmStatic
        private val polyglot = PolyglotImpl()

        @JvmStatic
        fun createSource(): Source =
            Source.newBuilder("js",
                "(function(record) {console.log('input:' + record.getId());})",
                "code.js").build()
    }
}