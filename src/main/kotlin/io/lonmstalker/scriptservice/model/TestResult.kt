package io.lonmstalker.scriptservice.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TestResult(
    internal var runtimeCodeTime: Long? = null,
    internal var compiledCodeTime: Long? = null,
    internal var newContextRuntimeCodeTime: Long? = null,
    internal var threadLocalCompiledCodeTime: Long? = null
)