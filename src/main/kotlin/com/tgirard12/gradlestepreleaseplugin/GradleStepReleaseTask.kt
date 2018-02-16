package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


open class GradleStepReleaseTask : DefaultTask() {

    val extension: GradleStepReleaseExtension by lazy {
        project.extensions.getByType(GradleStepReleaseExtension::class.java)
    }

    @TaskAction
    fun gradleStepReleaseTask() {
        
    }
}