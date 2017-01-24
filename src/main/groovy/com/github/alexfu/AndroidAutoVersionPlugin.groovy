package com.github.alexfu

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    enum VersionType {
        MAJOR("Major"), MINOR("Minor"), PATCH("Patch"), NONE("")
        static all() {
            return [MAJOR, MINOR, PATCH, NONE]
        }

        private final String name

        private VersionType(String name) {
            this.name = name
        }
    }

    enum VersionFlavor {
        RELEASE("Release"), BETA("Beta")

        private final String name

        private VersionFlavor(String name) {
            this.name = name
        }
    }

    private AndroidAutoVersionExtension extension

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
        def task = null

        if (flavor == VersionFlavor.RELEASE) {
            task = project.getTasks().findByName(extension.releaseTask)
        } else if (flavor == VersionFlavor.BETA) {
            task = project.getTasks().findByName(extension.betaReleaseTask)
        }

        if (task == null) {
            println("AndroidAutoVersionPlugin: Unable to find release task for: $type $flavor")
            return
        }

        task.mustRunAfter prepTask
        dependencies.add(task)

        project.task(name, {
            dependsOn dependencies
        })
    }

    private makePrepareTask(Project project, VersionType type, VersionFlavor flavor) {
        def name = "prepare"
        if (flavor == VersionFlavor.BETA) {
            name += flavor.name
        }
        name += type.name
        return project.task(name) << {
            def version = extension.getVersion()
            version.update(type)

            // Save new version
            extension.saveVersion(version)

            // Apply to all variants
            applyVersion(project, flavor)
        }
    }

    private applyVersion(Project project, VersionFlavor flavor = VersionFlavor.RELEASE) {
        project.android.applicationVariants.all { variant ->
            def versionCode = extension.getVersion().versionCode()
            def versionName = extension.getVersion().versionNameForFlavor(flavor)
            variant.mergedFlavor.versionCode = versionCode
            variant.mergedFlavor.versionName = versionName
        }
    }
}
