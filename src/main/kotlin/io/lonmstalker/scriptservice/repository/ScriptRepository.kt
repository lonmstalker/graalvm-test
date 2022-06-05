package io.lonmstalker.scriptservice.repository

import io.lonmstalker.scriptservice.model.Script
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.UUID

interface ScriptRepository : CoroutineCrudRepository<Script, UUID> {
}