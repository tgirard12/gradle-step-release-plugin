package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild


open class GradleStepReleasePlugin : Plugin<Project> {

    val rootTaskName = "releaseStep"
    val groupName = "releaseStep"

    override fun apply(project: Project) {

        // Add the extension object
        project.extensions.create(rootTaskName, GradleStepReleaseExtension::class.java)
        project.tasks.create(rootTaskName, GradleStepReleaseTask::class.java) { task ->

            task.group = groupName
            task.description = "Custom release steps"

            project.afterEvaluate { proj ->
                val extension = proj.extensions.getByType(GradleStepReleaseExtension::class.java)
                extension.project = proj

                val steps = extension.steps
                val myTasks = mutableListOf<Task>()

                steps.forEachIndexed { index, step ->
                    when (step) {
                        is CustomTask -> {
                            proj.tasks.create("${index.index()}_${step.title}") { task ->
                                task.group = groupName

                                task.doFirst {
                                    step.validation?.let { validation ->
                                        validation.beforeMessage.invoke()
                                        question("${validation.message} [y, yes]")
                                    }
                                }
                                task.doLast {
                                    step.stepResult = step.step()
                                }
                            }
                        }
                        is OtherTask -> {
                            proj.tasks.create(
                                "${index.index()}_${step.name.split(":").joinToString(separator = "_")}",
                                GradleBuild::class.java
                            ) { task ->
                                task.group = groupName
                                task.startParameter = proj.gradle.startParameter.newInstance().apply {
                                    setTaskNames(listOf(step.name))
                                }
                            }
                        }
                    }.let { myTasks += it }
                }

                myTasks.forEachIndexed { index, task ->
                    if (index > 0)
                        task.setShouldRunAfter(listOf(myTasks[index - 1].name))
                }
                task.setDependsOn(listOf(myTasks.map { it.name }))
            }
        }
    }
}

fun Int.index() = when (this) {
    in 0..9 -> "0$this"
    else -> this.toString()
}

fun question(question: String) {
    println(question)
    val line = readLine()
    if (line != "y" && line != "yes")
        throw IllegalArgumentException("question fail, Exit")
}