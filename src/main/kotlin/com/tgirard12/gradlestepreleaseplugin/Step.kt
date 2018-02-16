package com.tgirard12.gradlestepreleaseplugin


/**
 *
 */
data class Step(
        val title: String,
        val validation: Validation? = null,
        val step: () -> Any? = { null }

) {
    data class Validation(
            val message: String,
            val beforeMessage: () -> Unit = {}
    )
}