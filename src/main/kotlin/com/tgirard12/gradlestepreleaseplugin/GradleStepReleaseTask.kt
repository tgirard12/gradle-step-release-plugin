package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


open class GradleStepReleaseTask : DefaultTask() {

    val extension: GradleStepReleaseExtension by lazy {
        project.extensions.getByType(GradleStepReleaseExtension::class.java)
    }

    @TaskAction
    fun gradleStepReleaseTask() {
        if (extension.steps.isEmpty())
            throw IllegalArgumentException(
                """You must define at least one Step in releaseStep {
                        |   steps = [...]
                        |}""".trimMargin()
            )

        extension.steps.forEachIndexed { index, step ->
            "## Step : ${step.title}".println()
            "".println()
            step.validation?.let {
                step.validation.beforeMessage.invoke()
                "${step.validation.message} [y, yes]".question()
            }
            step.step()?.let { extension.stepResult.put(index, it) }
            "".println()
        }
    }
}
