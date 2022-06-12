package io.lonmstalker.scriptservice.model

import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("scripts")
data class Script(
    internal var id: UUID? = null,
    internal var title: String? = null,
    internal var value: String? = null
)
