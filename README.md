# gradle-step-release-plugin
Gradle plugin to launch release steps in custom order

[![Download](https://api.bintray.com/packages/tgirard12/kotlin/gradle-step-release-plugin/images/download.svg) ](https://bintray.com/tgirard12/kotlin/gradle-step-release-plugin/_latestVersion)


## Download

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
```


## Usage

```gradle
releaseStep {

    githubGroup = "tgirard12"
    githubProject = "gradle-step-release-plugin"

    steps = [
            question("Release ?")
            
            setProperties(["project_version"], "gradle.properties"),
            gitCheckout("master"),
            
            gitAdd(["gradle.properties"]),
            gitCommit("v${stepResult[1]}"),
            gitPush("origin", "master"),
            
            githubRelease(),
            
            question("Bintray publish done ?")
    ]
}
```


## Available Options

### Simple step

- `question("Release")`
- `step { println("Custom step") }`


### Properties files

- `checkProperties(propsKeys: List<String>, propFile: String = gradle.properties)`
    - Show properties in a file
    - `propsKeys` = Properties list to show
    - `propFile` = Properties file
    
- `setProperties(propsKeys: List<String>, propFile: String = gradle.properties)`
    - Modify properties in propFile without modify the order of any line
    - `propsKeys` = Properties list to modify
    - `propFile` = Properties file
    

### Git Step

- `gitCheckout(branch: String)`
- `gitAdd(files: List<String>)`
- `gitCommit(message: String)`
- `gitMerge(remote: String = "origin", branch: String)`
- `gitPull(remote: String = "origin", branch: String)`
- `gitPush(remote: String = "origin", branch: String)`
- `gitTag(name: String, message: String? = null)`


### GitHub Step

GitHub properties must be set :

- `githubUrl: String = "https://github.com"`
- `githubGroup: String`
- `githubProject: String`

Open Github Web UI :

- `githubPullRequest(sourceBranch: String, targetBranch: String)`
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

- `gitlabMergeRequest(sourceBranch: String, targetBranch: String)`
    - Create Merge request
- `gitlabTag(tagName: String, branch: String)`
    - Create new tag
- `gitlabMilestone()`
    - Create new Milestone
