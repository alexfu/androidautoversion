package com.github.alexfu

import org.gradle.api.Nullable
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private AndroidAutoVersionExtension extension
    private Version version
    private FlavorConfig releaseConfig
    @Nullable private FlavorConfig betaConfig

    @Override
    void apply(Project project) {
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)

        project.afterEvaluate {
            extension = project.androidAutoVersion
            releaseConfig = extension.releaseConfig()
            betaConfig = extension.betaConfig()

            // Check extension properties
            if (releaseConfig == null) {
                throw new IllegalArgumentException("release config must be defined for androidAutoVersion.")
            }
            releaseConfig.verify()

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

        if (betaConfig != null) {
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
            releaseTask = project.getTasks().findByName(releaseConfig.releaseTask)
        } else if (flavor == VersionFlavor.BETA) {
            releaseTask = project.getTasks().findByName(betaConfig.releaseTask)
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

            // Run global post hooks first
            extension.postHooks.each { hook ->
                hook(versionString)
            }

            if (flavor == VersionFlavor.RELEASE) {
                releaseConfig.postHooks.each { hook ->
                    hook(versionString)
                }
            } else if (flavor == VersionFlavor.BETA && betaConfig != null) {
                betaConfig.postHooks.each { hook ->
                    hook(versionString)
                }
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
