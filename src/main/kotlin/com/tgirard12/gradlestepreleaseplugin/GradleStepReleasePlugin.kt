package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.Plugin
import org.gradle.api.Project


open class GradleStepReleasePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        // Add the extension object
        project.extensions.create("releaseStep", GradleStepReleaseExtension::class.java)
        val task = project.tasks.create("releaseStep", GradleStepReleaseTask::class.java)

        task.group = "release"
        task.description = "Custom release steps"
    }
}