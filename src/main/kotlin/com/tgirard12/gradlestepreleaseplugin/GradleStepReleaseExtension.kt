package com.tgirard12.gradlestepreleaseplugin

import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


@Suppress("unused")
open class GradleStepReleaseExtension {

    // Const
    val gradleProperties = "gradle.properties"

    var stepResult = mutableMapOf<Int, Any?>()
    var steps = mutableListOf<Step>()

    // Simples

    fun question(message: String) = Step(
        title = "Question",
        validation = Step.Validation(message),
        step = { null }
    )

    fun step(title: String = "step", step: () -> Any?) = Step(
        title = title,
        step = step
    )

    // Properties

    fun checkProperties(propsKeys: List<String>, propFile: String = gradleProperties) = Step(
        title = "Check properties",
        validation = Step.Validation("All is OK") {
            Properties().apply {
                File(propFile)
                    .inputStream()
                    .use { load(it) }
            }
                .filterKeys { it -> propsKeys.contains(it) }
                .map { k -> "${k.key}=${k.value}" }
                .sorted()
                .joinToString("\n", postfix = "\n")
                .println()
        },
        step = {}
    )

    fun setProperties(propsKeys: List<String>, propFile: String = gradleProperties) = Step(
        title = "Set properties",
        step = {
            "New value to update ?".println()
            read()?.let { input ->
                val lines = File(propFile)
                    .readLines()

                val mutLines = lines.toMutableList()

                propsKeys.forEach { prop ->
                    lines
                        .indexOfFirst { it.split("=")[0] == prop }
                        .takeIf { it > -1 }
                        ?.let {
                            mutLines.set(it, lines[it].replaceAfterLast("=", input))
                        }
                }

                File(propFile).writeText(mutLines.joinToString("\n"))

                return@let input
            }
        }
    )

    // Git Action

    fun gitCheckout(branch: String) = Step(
        title = "git checkout",
        step = { "git checkout $branch".exec() }
    )

    fun gitAdd(files: List<String>) = Step(
        title = "git add",
        step = { "git add ${files.joinToString(separator = " ")}".exec() }
    )

    fun gitCommit(message: String) = Step(
        title = "git commit",
        step = { """git commit -m "$message" """.exec() }
    )

    fun gitMerge(remote: String = "origin", branch: String) = Step(
        title = "git merge",
        step = { "git merge $remote $branch".exec() }
    )

    fun gitPull(remote: String = "origin", branch: String) = Step(
        title = "git pull",
        step = { "git pull $remote $branch".exec() }
    )

    fun gitPush(remote: String = "origin", branch: String) = Step(
        title = "git push",
        step = { "git push $remote $branch".exec() }
    )

    fun gitTag(name: String, message: String? = null) = Step(
        title = """git tag""",
        step = { """git tag ${message?.let { "-m $message - a" }} $name""".exec() }
    )

    // Gitlab Action

    var gitlabUrl: String = "https://gitlab.com"
    var gitlabGroup: String? = null
    var gitlabProject: String? = null

    fun gitlabMergeRequest(sourceBranch: String, targetBranch: String) = Step(
        title = "GitLab Merge Request UI",
        validation = Step.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/merge_requests/new?utf8=%E2%9C%93" +
                    "&merge_request%5Bsource_branch%5D=$sourceBranch" +
                    "&merge_request%5Btarget_branch%5D=$targetBranch" +
                    "\n\nMerge Request merged ?"
        )
    )

    fun gitlabTag(tagName: String, branch: String) = Step(
        title = "Gitlab tag UI",
        validation = Step.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/tags/new?" +
                    "tag_name=$tagName&ref=$branch" +
                    "\n\nTag Created ?"
        )
    )

    fun gitlabMilestone() = Step(
        title = "Gitlab Milestone UI",
        validation = Step.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/milestones/new" +
                    "\n\nMilestone Created ?"
        )
    )

    // GitHub Action

    var githubUrl: String = "https://github.com"
    var githubGroup: String? = null
    var githubProject: String? = null

    fun githubPullRequest(sourceBranch: String, targetBranch: String) = Step(
        title = "GitHub Pull Request UI",
        validation = Step.Validation(
            "$githubUrl/$githubGroup/$githubProject/compare/$targetBranch...$sourceBranch" +
                    "\n\nPull Request merged ?"
        )
    )

    fun githubRelease() = Step(
        title = "GitHub Release UI",
        validation = Step.Validation(
            "$githubUrl/$githubGroup/$githubProject/releases/new" +
                    "\n\nRelease Created ?"
        )
    )

    fun githubMilestone() = Step(
        title = "GitHub Milestone UI",
        validation = Step.Validation(
            "$githubUrl/$githubGroup/$githubProject/milestones/new" +
                    "\n\nMilestone Created ?"
        )
    )
}

// Common functions

fun String.println(): Unit = println(this)
fun String.print(): Unit = print(this)

fun read() = readLine().apply { println(this) }

fun String.question() {
    this.println()
    "$< ".print()
    val line = read()
    if (line != "y" && line != "yes")
        throw IllegalArgumentException("question fail, Exit")
}

fun String.exec(): String? {
    try {
        "$> $this".println()
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        proc.waitFor(10, TimeUnit.SECONDS)

        val output: String? = proc.inputStream
            .bufferedReader()
            .readText()
            .takeIf { it.isNotBlank() }
            ?.apply { this.println() }

        proc.errorStream
            .bufferedReader()
            .readText()
            .takeIf { it.isNotBlank() }
            ?.apply { "ERROR => " + this.println() }

        proc.exitValue()
            .takeIf { it != 0 }
            ?.let {
                println("ERROR => EXIT with `$it`")
                exitProcess(it)
            }
        return output?.trim()

    } catch (ex: Exception) {
        ex.printStackTrace()
        exitProcess(-1)
    }
}
