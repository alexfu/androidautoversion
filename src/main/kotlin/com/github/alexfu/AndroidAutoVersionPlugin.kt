package com.github.alexfu

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

private typealias VersionFile = File
private const val LOG_TAG = "[AndroidAutoVersion]"

class AndroidAutoVersionPlugin : Plugin<Project> {
    private lateinit var project: Project
    private val json by lazy { Json(JsonConfiguration.Stable) }
    private val versionFile by lazy { project.file("version") }
    private val version by lazy { versionFile.read() }

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
        if (!versionFile.exists()) {
            info("Version file does not exist, auto generating a default one.")
            Version().writeTo(versionFile)
        }
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension::class.java, version.versionName, version.buildNumber)
    }

    private fun createTasks() {
        registerTask(name = "bumpPatch", description = "Increases patch version by 1") {
            version.update(VersionType.PATCH).writeTo(versionFile)
        }
        registerTask(name = "bumpMinor", description = "Increases minor version by 1 and zeroes out patch version") {
            version.update(VersionType.MINOR).writeTo(versionFile)
        }
        registerTask(name = "bumpMajor", description = "Increases major version by 1, zeroes out minor and patch version") {
            version.update(VersionType.MAJOR).writeTo(versionFile)
        }
    }

    private fun registerTask(name: String, description: String, exec: () -> Unit) {
        project.tasks.register(name) {
            this.group = "AndroidAutoVersion"
            this.description = description
            doLast {
                exec()
            }
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
