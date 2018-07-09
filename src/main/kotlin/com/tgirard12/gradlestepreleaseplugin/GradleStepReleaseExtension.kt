package com.tgirard12.gradlestepreleaseplugin

import org.gradle.api.Project
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


@Suppress("unused")
open class GradleStepReleaseExtension {

    val log = LoggerFactory.getLogger(GradleStepReleaseExtension::class.java)
    internal lateinit var project: Project

    // Const
    val gradleProperties = "gradle.properties"

    var steps = mutableListOf<StepTask>()

    // Simples

    fun message(message: () -> String) = CustomTask(
        title = "message",
        step = {
            message().println()
            Unit
        }
    )

    fun question(message: () -> String) = CustomTask(
        title = "question",
        validation = StepTask.Validation(message()),
        step = { Unit }
    )

    fun read(message: () -> String) = CustomTask(
        title = "read",
        step = {
            message().println()
            readLine()
        }
    )

    fun step(title: String = "step", step: () -> Any?) = CustomTask(
        title = title,
        step = step
    )

    fun step(step: () -> Any?) = CustomTask(
        title = "step",
        step = step
    )

    fun task(name: String) = OtherTask(
        name = name
    )

    // Properties

    fun checkProperties(propsKeys: List<String>, propFile: String = gradleProperties) = CustomTask(
        title = "checkProperties",
        validation = StepTask.Validation("All is OK") {
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
        step = { Unit }
    )

    fun setProperties(propsKeys: List<String>, propFile: String = gradleProperties) = CustomTask(
        title = "setProperties",
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

    fun gitCheckout(branch: () -> String) = CustomTask(
        title = "gitCheckout",
        step = { exec("git", listOf("checkout", branch())) }
    )

    fun gitAdd(files: () -> List<String>) = CustomTask(
        title = "gitAdd",
        step = {
            files().forEach { file ->
                exec("git", listOf("add", file))
            }
        }
    )

    fun gitCommit(message: () -> String) = CustomTask(
        title = "gitCommit",
        step = { exec("git", listOf("commit", "-m", message())) }
    )

    fun gitMerge(remote: String = "origin", branch: String) = CustomTask(
        title = "gitMerge",
        step = { exec("git", listOf("merge", remote, branch)) }
    )

    fun gitPull(remote: String = "origin", branch: String) = CustomTask(
        title = "gitPull",
        step = { exec("git", listOf("pull", remote, branch)) }
    )

    fun gitPush(remote: String = "origin", branch: String) = CustomTask(
        title = "gitPush",
        step = { exec("git", listOf("push", remote, branch)) }
    )

    fun gitTag(name: () -> String, message: () -> String? = { null }) = CustomTask(
        title = "gitTag",
        step = {
            val args = mutableListOf("tag", "-a", name())
            message()?.let {
                args.add("-m")
                args.add(it)
            }
            exec("git", args)
        }
    )

    // Gitlab Action

    var gitlabUrl: String = "https://gitlab.com"
    var gitlabGroup: String? = null
    var gitlabProject: String? = null

    fun gitlabMergeRequest(sourceBranch: String, targetBranch: String) = CustomTask(
        title = "gitLabMergeRequest",
        validation = StepTask.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/merge_requests/new?utf8=%E2%9C%93" +
                    "&merge_request%5Bsource_branch%5D=$sourceBranch" +
                    "&merge_request%5Btarget_branch%5D=$targetBranch" +
                    "\n\nMerge Request merged ?"
        )
    )

    fun gitlabTag(tagName: () -> String, branch: String) = CustomTask(
        title = "gitlabTag",
        validation = StepTask.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/tags/new?" +
                    "tag_name=${tagName()}&ref=$branch" +
                    "\n\nTag Created ?"
        )
    )

    fun gitlabMilestone() = CustomTask(
        title = "gitlabMilestone",
        validation = StepTask.Validation(
            "$gitlabUrl/$gitlabGroup/$gitlabProject/milestones/new" +
                    "\n\nMilestone Created ?"
        )
    )

    // GitHub Action

    var githubUrl: String = "https://github.com"
    var githubGroup: String? = null
    var githubProject: String? = null

    fun githubPullRequest(sourceBranch: String, targetBranch: String) = CustomTask(
        title = "gitHubPullRequest",
        validation = StepTask.Validation(
            "$githubUrl/$githubGroup/$githubProject/compare/$targetBranch...$sourceBranch" +
                    "\n\nPull Request merged ?"
        )
    )

    fun githubRelease() = CustomTask(
        title = "gitHubRelease",
        validation = StepTask.Validation(
            "$githubUrl/$githubGroup/$githubProject/releases/new" +
                    "\n\nRelease Created ?"
        )
    )

    fun githubMilestone() = CustomTask(
        title = "gitHubMilestone",
        validation = StepTask.Validation(
            "$githubUrl/$githubGroup/$githubProject/milestones/new" +
                    "\n\nMilestone Created ?"
        )
    )

// Common functions

    fun String.println(): Unit = println(this)
    fun String.print(): Unit = print(this)

    fun read() = readLine()

    fun String.question() = question(this)

    data class ExecRes(
        val exitValue: Int,
        val input: String?,
        val error: String?
    )

    private fun exec(command: String, args: List<String>): ExecRes {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val res = project.exec { exec ->
            exec.standardOutput = stdout
            exec.errorOutput = stderr
            exec.workingDir = project.rootDir
            exec.commandLine = listOf(command)
            exec.args = args
        }.exitValue
        return ExecRes(res, stdout.toString(), stderr.toString())
    }
}