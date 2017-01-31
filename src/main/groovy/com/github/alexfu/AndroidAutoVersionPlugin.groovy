package com.github.alexfu

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private AndroidAutoVersionExtension extension
    private Version version

    @Override
    void apply(Project project) {
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)

        project.afterEvaluate {
            extension = project.androidAutoVersion

            // Check extension properties
            if (extension.releaseTask == null) {
                throw new IllegalArgumentException("releaseTask must be defined for androidAutoVersion.")
            }
            if (extension.versionFile == null) {
                throw new IllegalArgumentException("versionFile must be defined for androidAutoVersion.")
            }

            if (extension.versionFile.exists()) {
                version = new Version(extension.versionFile, extension.versionFormatter)
            } else {
                version = new Version(extension.versionFormatter)
                extension.versionFile.write(version.toJson())
            }

            applyVersion(project)
            makeReleaseTasks(project)
        }
    }

    private makeReleaseTasks(Project project) {
        def types = VersionType.all()
        def flavors = [VersionFlavor.RELEASE]

        if (extension.betaReleaseTask != null) {
            flavors.add(VersionFlavor.BETA)
        }

        def tasks = []
        for (flavor in flavors) {
            for (type in types) {
                tasks.add(makeReleaseTask(project, type, flavor))
            }
        }

        return tasks
    }

    private makeReleaseTask(Project project, VersionType type, VersionFlavor flavor) {
        // Generate task name
        def name = "release"
        if (flavor == VersionFlavor.BETA) {
            name += flavor.name
        }
        name += type.name

        // Set up dependencies on release task
        def prepTask = makePrepareTask(project, type, flavor)
        def dependencies = [prepTask]
        def releaseTask = null

        if (flavor == VersionFlavor.RELEASE) {
            releaseTask = project.getTasks().findByName(extension.releaseTask)
        } else if (flavor == VersionFlavor.BETA) {
            releaseTask = project.getTasks().findByName(extension.betaReleaseTask)
        }

        if (releaseTask == null) {
            println("AndroidAutoVersionPlugin: Unable to find release task for: $type $flavor")
            return
        }

        releaseTask.mustRunAfter prepTask
        dependencies.add(releaseTask)

        def task = project.task(name, {
            dependsOn dependencies
        })

        task.doLast {
            def versionString = version.versionNameForFlavor(flavor)
            extension.postHooks.each { hook ->
                hook(versionString)
            }
        }

        return task
    }

    private makePrepareTask(Project project, VersionType type, VersionFlavor flavor) {
        def name = "prepare"
        if (flavor == VersionFlavor.BETA) {
            name += flavor.name
        }
        name += type.name
        return project.task(name) << {
            version.update(type)

            // Save new version
            extension.versionFile.write(version.toJson())

            // Apply to all variants
            applyVersion(project, flavor)
        }
    }

    private applyVersion(Project project, VersionFlavor flavor = VersionFlavor.RELEASE) {
        project.android.applicationVariants.all { variant ->
            def versionCode = version.versionCode()
            def versionName = version.versionNameForFlavor(flavor)
            variant.mergedFlavor.versionCode = versionCode
            variant.mergedFlavor.versionName = versionName
        }
    }
}
