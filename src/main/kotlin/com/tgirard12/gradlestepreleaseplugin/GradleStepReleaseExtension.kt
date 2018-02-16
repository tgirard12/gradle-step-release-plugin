package com.tgirard12.gradlestepreleaseplugin

import java.io.File
import java.util.*


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
                        .console()
            },
            step = {}
    )

    fun setProperties(propsKeys: List<String>, propFile: String = gradleProperties) = Step(
            title = "Set properties",
            step = {
                "New value to update ?".console()
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
            step = { "git checkout $branch".runCommand() }
    )

    fun gitAdd(files: List<String>) = Step(
            title = "git add",
            step = { "git add ${files.joinToString(separator = " ")}".runCommand() }
    )

    fun gitCommit(message: String) = Step(
            title = "git commit",
            step = { """git commit -m "$message" """.runCommand() }
    )

    fun gitMerge(remote: String = "origin", branch: String) = Step(
            title = "git merge",
            step = { "git merge $remote $branch".runCommand() }
    )

    fun gitPull(remote: String = "origin", branch: String) = Step(
            title = "git merge",
            step = { "git merge $remote $branch".runCommand() }
    )

    fun gitPush(remote: String = "origin", branch: String) = Step(
            title = "git push",
            step = { "git push $remote $branch".runCommand() }
    )

    fun gitTag(name: String, message: String? = null) = Step(
            title = """git tag""",
            step = { """git tag ${message?.let { "-m $message - a" }} $name""".runCommand() }
    )

    // Gitlab Action

    var gitlabUrl: String? = null
    var gitlabGroup: String? = null
    var gitlabProject: String? = null

    fun gitlabMergeRequest(sourceBranch: String, targetBranch: String) = Step(
            title = "GitLab Merge Request UI",
            validation = Step.Validation("$gitlabUrl/$gitlabGroup/$gitlabProject/merge_requests/new?utf8=%E2%9C%93" +
                    "&merge_request%5Bsource_branch%5D=$sourceBranch" +
                    "&merge_request%5Btarget_branch%5D=$targetBranch" +
                    "\n\nMerge Request done ?")
    )

    fun gitlabTag(tagName: String, branch: String) = Step(
            title = "Gitlab tag UI",
            validation = Step.Validation("$gitlabUrl/$gitlabGroup/$gitlabProject/tags/new?" +
                    "tag_name=$tagName&ref=$branch" +
                    "\n\nTag Created ?")
    )

    fun gitlabMilestone() = Step(
            title = "Gitlab Milestone UI",
            validation = Step.Validation("$gitlabUrl/$gitlabGroup/$gitlabProject/milestones/new" +
                    "\n\nMilestone Created ?")
    )
}