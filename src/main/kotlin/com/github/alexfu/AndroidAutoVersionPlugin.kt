package com.github.alexfu

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.github.alexfu.VersionType.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

private typealias VersionFile = File
private const val LOG_TAG = "[AndroidAutoVersion]"

class AndroidAutoVersionPlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var version: Version
    private lateinit var versionFile: VersionFile
    private val json by lazy { Json(JsonConfiguration.Stable) }

    override fun apply(project: Project) {
        this.project = project
        val isAppPlugin = project.plugins.findPlugin(AppPlugin::class.java) != null
        val isLibraryPlugin = project.plugins.findPlugin(LibraryPlugin::class.java) != null
        check(!isAppPlugin || !isLibraryPlugin) {
            "'com.android.application' or 'com.android.library' plugin required."
        }
        setUp()
        createTasks()
    }

    private fun setUp() {
        versionFile = project.file("version")
        if (!versionFile.exists()) {
            info("Version file does not exist, auto generating a default one.")
            Version().writeTo(versionFile)
        }
        version = versionFile.read()
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension::class.java, version.versionName, version.buildNumber)
    }

    private fun createTasks() {
        registerTask(
            name = "bumpBuild",
            description = "Increases build number by 1",
            exec = { incrementVersion(BUILD) }
        )

        val bumpPatchTask = registerTask(
            name = "bumpPatch",
            description = "Increases patch version by 1",
            exec = { incrementVersion(PATCH) }
        )

        val bumpMinorTask = registerTask(
            name = "bumpMinor",
            description = "Increases minor version by 1 and zeroes out patch version",
            exec = { incrementVersion(MINOR) }
        )

        val bumpMajorTask = registerTask(
            name = "bumpMajor",
            description = "Increases major version by 1, zeroes out minor and patch version",
            exec = { incrementVersion(MAJOR) }
        )
        
        registerTask(
            name = "versionBuild",
            description = "Executes bumpBuild and commits the changes to git",
            dependencies = listOf(bumpBuild),
            exec = ::commitToGit
        )

        registerTask(
            name = "versionPatch",
            description = "Executes bumpPatch and commits the changes to git",
            dependencies = listOf(bumpPatchTask),
            exec = ::commitToGit
        )

        registerTask(
            name = "versionMinor",
            description = "Executes bumpMinor and commits the changes to git",
            dependencies = listOf(bumpMinorTask),
            exec = ::commitToGit
        )

        registerTask(
            name = "versionMajor",
            description = "Executes bumpMajor and commits the changes to git",
            dependencies = listOf(bumpMajorTask),
            exec = ::commitToGit
        )
    }

    private fun incrementVersion(type: VersionType) {
        version = version.increment(type)
        version.writeTo(versionFile)
    }

    private fun commitToGit() {
        project.exec { setCommandLine("git", "add", versionFile.absolutePath) }
        project.exec { setCommandLine("git", "commit", "-m", "Update to $version") }
        project.exec { setCommandLine("git", "tag", "v$version") }
    }

    private fun registerTask(name: String, description: String, dependencies: Iterable<TaskProvider<Task>>? = null, exec: () -> Unit): TaskProvider<Task> {
        return project.tasks.register(name) {
            group = "AndroidAutoVersion"
            setDescription(description)
            if (dependencies != null) {
                setDependsOn(dependencies)
            }
            doLast { exec() }
        }
    }

    private fun warn(message: String, error: Throwable?) {
        if (error == null) {
            project.logger.warn("$LOG_TAG $message")
        } else {
            project.logger.warn("$LOG_TAG $message", error)
        }
    }

    private fun info(message: String) {
        project.logger.info("$LOG_TAG $message")
    }

    private fun VersionFile.read(): Version {
        if (!exists()) return Version()
        return try {
            json.parse(Version.serializer(), readText())
        } catch (error: Error) {
            warn("Error reading version file, falling back to default", error)
            Version()
        }
    }

    private fun Version.writeTo(file: VersionFile) {
        file.writeText(json.stringify(Version.serializer(), this))
    }
}
