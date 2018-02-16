package com.tgirard12.gradlestepreleaseplugin


open class GradleStepReleaseExtension {

    var stepResult = mutableMapOf<Int, Any?>()
    var steps = mutableListOf<Step>()


    fun question(message: String) = Step(
            title = "Question",
            validation = Step.Validation(message),
            step = { null }
    )

    fun step(title: String = "step", step: () -> Any?) = Step(
            title = title,
            step = step
    )


}