package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory


open class GradleStepReleaseTask : DefaultTask() {

    val log = LoggerFactory.getLogger(GradleStepReleaseTask::class.java)

    val extension: GradleStepReleaseExtension by lazy {
        val ext = project.extensions.getByType(GradleStepReleaseExtension::class.java)
        ext.project = project
        ext
    }

    @TaskAction
    fun gradleStepReleaseTask() {
        if (extension.steps.isEmpty())
            log.info(
                """releaseStep.stepResult : You must define at least one Step in releaseStep {
                        |   steps = [...]
                        |}""".trimMargin()
            )
    }
}
