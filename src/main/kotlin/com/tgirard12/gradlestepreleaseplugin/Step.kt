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
