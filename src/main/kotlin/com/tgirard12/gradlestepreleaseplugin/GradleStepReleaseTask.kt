package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException
import java.util.concurrent.TimeUnit


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
                        |}""".trimMargin())

        extension.steps.forEachIndexed { index, step ->
            "## Step : ${step.title}".console()
            "".console()
            step.validation?.let {
                step.validation.beforeMessage.invoke()
                "${step.validation.message} [y, yes] ?".validateQuestion()
            }
            step.step()?.let { extension.stepResult.put(index, it) }
            "".console()
        }
    }
}

fun read() = readLine().apply { this?.console() }

fun String.validateQuestion() {
    this.console()
    val line = read()
    if (line != "y" && line != "yes")
        throw IllegalArgumentException("Validation fail, Exit task ")
}

fun String.runCommand() {
    try {
        "$> $this".console()
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        proc.waitFor(10, TimeUnit.SECONDS)
        proc.inputStream.bufferedReader().readText().console()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun String.console(): Unit = println(this)
