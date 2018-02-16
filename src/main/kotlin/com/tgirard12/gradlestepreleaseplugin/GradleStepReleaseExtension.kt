package com.tgirard12.gradlestepreleaseplugin

import java.io.File
import java.util.*


open class GradleStepReleaseExtension {

    // Const
    val gradleProperties = "gradle.properties"

    var stepResult = mutableMapOf<Int, Any?>()
    var steps = mutableListOf<Step>()

    // Simples

    fun question(message: String) = Step(
            title = "Question",
            validation = Step.Validation(message),
            step = { null }
    )

    fun step(title: String = "step", step: () -> Any?) = Step(
            title = title,
            step = step
    )

    // Properties

    fun checkProperties(propsKeys: List<String>, propFile: String = gradleProperties) = Step(
            title = "Check properties",
            validation = Step.Validation("All is OK") {
                Properties().apply {
                    File(propFile)
                            .inputStream()
                            .use { load(it) }
                }
                        .filterKeys { it -> propsKeys.contains(it) }
                        .map { k -> "${k.key}=${k.value}" }
                        .sorted()
                        .joinToString("\n", postfix = "\n")
                        .console()
            },
            step = {}
    )
}