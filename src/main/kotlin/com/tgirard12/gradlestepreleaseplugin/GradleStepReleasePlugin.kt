package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task


open class GradleStepReleasePlugin : Plugin<Project> {

    val rootTaskName = "releaseStep"
    val groupName = "releaseStep"

    override fun apply(project: Project) {

        // Add the extension object
        project.extensions.create(rootTaskName, GradleStepReleaseExtension::class.java)
        val task = project.tasks.create(rootTaskName, GradleStepReleaseTask::class.java)

        task.group = groupName
        task.description = "Custom release steps"

        project.afterEvaluate { proj ->
            val steps = proj.extensions.getByType(GradleStepReleaseExtension::class.java).steps
            val myTasks = mutableListOf<Task>()

            steps.forEachIndexed { index, step ->
                when (step) {
                    is CustomTask -> {
                        proj.tasks.create("${index}_${step.title}") { task ->
                            task.group = groupName

                            task.doFirst {
                                step.validation?.let { validation ->
                                    validation.beforeMessage.invoke()
                                    question("${validation.message} [y, yes]")
                                }
                                step.stepResult = step.step()
                            }
                        }
                    }
                }?.let { myTasks += it }
            }

            myTasks.forEachIndexed { index, task ->
                if (index > 0)
                    task.setMustRunAfter(listOf(myTasks[index - 1].name))
            }

            task.setFinalizedBy(listOf(myTasks.map { it.name }))
        }
    }
}

fun question(question: String) {
    println(question)
    val line = readLine()
    if (line != "y" && line != "yes")
        throw IllegalArgumentException("question fail, Exit")
}