# gradle-step-release-plugin
Gradle plugin to launch custom and gradle task in sequential order

[![Download](https://api.bintray.com/packages/tgirard12/kotlin/gradle-step-release-plugin/images/download.svg) ](https://bintray.com/tgirard12/kotlin/gradle-step-release-plugin/_latestVersion)


## Download and Usage

```gradle
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.tgirard12:gradle-step-release-plugin:VERSION"
    }
}

apply plugin: "com.tgirard12.gradle-step-release-plugin"

releaseStep {

    githubGroup = "tgirard12"
    githubProject = "gradle-step-release-plugin"

    def gradleFile = "gradle.properties"
    def props = ["project_version"]

    def releaseVersion = setProperties(props, gradleFile)

    steps = [message { "Releasing a new version" },

             gitCheckChanges(),
             gitCheckBranch { "master" },
             
             // Set new Release version
             releaseVersion,
             checkProperties(props, gradleFile),

             // Commit and create release version
             gitAdd { [gradleFile] },
             gitCommit { "v" + releaseVersion.stepResult },

             task(":clean"),
             task(":test"),

             // Publish on bintray
             task(":bintrayUpload"),
             question {
                 "Publish artifact on jcenter ? \n\n" +
                         "https://bintray.com/tgirard12/kotlin/gradle-step-release-plugin ?"
             },

             gitPush("origin", "master"),

             githubRelease(),
             githubMilestone(),

             message { "Release v" + releaseVersion.stepResult + " OK :-)" }
    ]
}
```


## Available Options

### Simple step

- `message { "Simple message" }`
- `question { "Release a new version ?" }`
- `read { "Read a value" }: String`
- `step { println("Custom step") }`


### Gradle task step

- `task(":clean")`
- `task(":project:build")`


### Properties files

- `checkProperties(propsKeys: List<String>, propFile: String = gradle.properties)`
    - Show properties in a file
    - `propsKeys` : Properties list to show
    - `propFile` : Properties file
    
- `setProperties(propsKeys: List<String>, propFile: String = gradle.properties)`
    - Modify properties in propFile without modify the order of any line
    - `propsKeys` : Properties list to modify
    - `propFile` : Properties file
    

### Git Step

- `gitCheckout { "branch" }`
- `gitAdd { ["files"] }`
- `gitCommit { "message" }`
- `gitMerge("origin", "master")`
- `gitPull("origin", "master")`
- `gitPush("origin", "master")`
- `gitTag { "name" } message: String? = null)`
- `gitCheckChanges()` : Check if the repository is clean
- `gitCheckBranch()` : Check the current branch


### GitHub Step

GitHub properties must be set :

- `githubUrl: String = "https://github.com"`
- `githubGroup: String`
- `githubProject: String`

Open Github Web UI :

- `githubPullRequest("sourceBranch", "targetBranch")`
    - Create Pull request
- `githubRelease()`
    - Create new release
- `githubMilestone()`
    - Create new Milestone


### GitLab Step

Gitlab properties must be set :

- `gitlabUrl: String = "https://gitlab.com"`
- `gitlabGroup: String?`
- `gitlabProject: String?`

Open GitLab Web UI :

- `gitlabMergeRequest("sourceBranch", "targetBranch")`
    - Create Merge request
- `gitlabTag( { "tagName" }, "branch")`
    - Create new tag
- `gitlabMilestone()`
    - Create new Milestone
