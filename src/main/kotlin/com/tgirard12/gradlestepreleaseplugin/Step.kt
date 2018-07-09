package com.tgirard12.gradlestepreleaseplugin

/**
 *
 */
sealed class StepTask(
    open val validation: Validation? = null,
    open val step: () -> Any? = { Unit }
) {
    var stepResult: Any? = null

    data class Validation(
        val message: String,
        val beforeMessage: () -> Unit = {}
    )
}

/**
 *
 */
data class CustomTask(
    val title: String,
    override val validation: Validation? = null,
    override val step: () -> Any? = { Unit }
) : StepTask(validation, step) {

}

/**
 *
 */
data class OtherTask(
    val name: String
) : StepTask()

fun OtherTask.taskName(): String {
    val split = this.name
        .trimStart(':')
        .split(":")
    return when (split.size) {
        1 -> split[0]
        2 -> split[1]
        else -> throw  IllegalArgumentException("taskName not found in `${this.name}`")
    }
}

fun OtherTask.projectName(): String? {
    val split = this.name
        .trimStart(':')
        .split(":")
    return when (split.size) {
        2 -> split[0]
        else -> null
    }
}
