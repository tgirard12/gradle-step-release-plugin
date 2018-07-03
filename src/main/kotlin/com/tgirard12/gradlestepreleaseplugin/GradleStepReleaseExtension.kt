package com.tgirard12.gradlestepreleaseplugin

import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


@Suppress("unused")
open class GradleStepReleaseExtension {

    val log = LoggerFactory.getLogger(GradleStepReleaseExtension::class.java)
    internal lateinit var project: Project

    // Const
    val gradleProperties = "gradle.properties"

    var steps = mutableListOf<Step>()

    // Simples

    fun message(message: String) = Step(
        title = "Message",
        step = {
            message.println()
            Unit
        }
    )

    fun message(message: () -> String) = Step(
        title = "Message",
        step = {
            message().println()
            Unit
        }
    )

    fun question(message: String) = Step(
        title = "Question",
        validation = Step.Validation(message),
        step = { Unit }
    )

    fun question(message: () -> String) = Step(
        title = "Question",
        validation = Step.Validation(message()),
        step = { Unit }
    )

    fun read(message: String) = Step(
        title = "Message",
        step = {
            message.println()
            readLine()
        }
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
        step = { Unit }
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

    fun gitCheckout(branch: () -> String) = Step(
        title = "git checkout",
        step = { "git checkout ${branch()}".exec().exitValue }
    )

    fun gitAdd(files: () -> List<String>) = Step(
        title = "git add",
        step = {
            files().forEach {
                "git add $it".exec().exitValue
            }
        }
    )

    fun gitCommit(message: () -> String) = Step(
        title = "git commit",
        step = { "git commit -m \"" + message() + "\" ".exec().exitValue }
    )

    fun gitMerge(remote: String = "origin", branch: String) = Step(
        title = "git merge",
        step = { "git merge $remote $branch".exec().exitValue }
    )

    fun gitPull(remote: String = "origin", branch: String) = Step(
        title = "git pull",
        step = { "git pull $remote $branch".exec().exitValue }
    )

    fun gitPush(remote: String = "origin", branch: String) = Step(
        title = "git push",
        step = { "git push $remote $branch".exec().exitValue }
    )

    fun gitTag(name: () -> String, message: () -> String? = { null }) = Step(
        title = """git tag""",
        step = { """git tag ${message()?.let { "-m ${message()} -a" }} ${name()}""".exec().exitValue }
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

    fun exec(command: String) = command.exec()
}

// Common functions

    fun String.println(): Unit = println(this)
    fun String.print(): Unit = print(this)

    fun read() = readLine()

    fun String.question() {
        this.println()
        val line = read()
        if (line != "y" && line != "yes")
            throw IllegalArgumentException("question fail, Exit")
    }

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