package io.lonmstalker.scriptservice.service

import io.lonmstalker.scriptservice.model.DomainObject
import org.graalvm.polyglot.Value
import java.util.UUID

interface ScriptService {
    fun executeRuntimeCode(id: UUID, data: DomainObject): Value
    fun executeCompiledCode(id: UUID, data: DomainObject): Value
    fun executeThreadLocalCompiledCode(id: UUID, data: DomainObject): Value
    fun executeWithNewContext(id: UUID, data: DomainObject): Value
}