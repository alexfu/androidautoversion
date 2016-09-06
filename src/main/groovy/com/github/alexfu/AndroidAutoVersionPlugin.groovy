package com.github.alexfu

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private enum VersionType {
        MAJOR("Major"), MINOR("Minor"), PATCH("Patch")
        static def all() {
            return [MAJOR, MINOR, PATCH]
        }

        private final String name;

        private VersionType(String name) {
            this.name = name;
        }
    }

    private enum VersionFlavor {
        RELEASE("Release"), BETA("Beta")
        static def all() {
            return [RELEASE, BETA]
        }

        private final String name;

        private VersionFlavor(String name) {
            this.name = name;
        }
    }

    private AndroidAutoVersionExtension extension

    @Override
    void apply(Project project) {
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)

        project.afterEvaluate {
            extension = project.androidAutoVersion
            applyVersion(project, extension.getVersion())
            makeReleaseTasks(project)
        }
    }

    private def makeReleaseTasks(Project project) {
        def types = VersionType.all()
        def flavors = VersionFlavor.all()

        def tasks = []
        for (def flavor in flavors) {
            for (def type in types) {
                tasks.add(makeReleaseTask(project, type, flavor))
            }
        }

        return tasks
    }

    private def makeReleaseTask(Project project, VersionType type, VersionFlavor flavor) {
        def name = "release"
        if (flavor == VersionFlavor.BETA) {
            name += flavor.name
        }
        name += type.name

        def prepTask = makePrepareTask(project, type, flavor)
        def dependencies = [prepTask.name]

        if (flavor == VersionFlavor.RELEASE) {
            def releaseTask = "assembleRelease"
            dependencies.add(releaseTask)
            def task = project.getTasks().getByName("assembleRelease")
            task.mustRunAfter(prepTask)
        }
        if (flavor == VersionFlavor.BETA) {
            def betaReleaseTask = extension.betaReleaseTask
            dependencies.add(betaReleaseTask)
            def task = project.getTasks().getByName(betaReleaseTask)
            task.mustRunAfter(prepTask)
        }

        project.task(name, {
            dependsOn dependencies
        })
    }

    private def makePrepareTask(Project project, VersionType type, VersionFlavor flavor) {
        def name = "prepare"
        if (flavor == VersionFlavor.BETA) {
            name += flavor.name
        }
        name += type.name
        return project.task(name) << {
            def version = extension.getVersion();
            def newVersion = updateVersion(version, type);

            // Save new version
            extension.saveVersion(newVersion)

            // Apply to all variants
            applyVersion(project, newVersion)

            def git = Grgit.open(project.rootDir)

            // Add changes
            def currentDir = new File(new File("").absolutePath) // File must use absolute path
            def versionFilePath = currentDir.toPath().relativize(extension.versionFile.toPath()).toString()
            git.add(update: true, patterns: [versionFilePath])

            // Commit
            git.commit(message: "Update version")

            // Tag
            if (flavor == VersionFlavor.BETA) {
                git.tag.add(name: "${newVersion.betaVersionName()}")
            } else {
                git.tag.add(name: "${newVersion.versionName()}")
            }
        }
    }

    private def updateVersion(Version version, VersionType type) {
        Version newVersion = new Version(version)
        newVersion.buildNumber += 1
        switch (type) {
            case VersionType.MAJOR:
                newVersion.patch = 0
                newVersion.minor = 0
                newVersion.major += 1
                break;
            case VersionType.MINOR:
                newVersion.patch = 0
                newVersion.minor += 1
                break;
            case VersionType.PATCH:
                newVersion.patch += 1
                break;
        }
        return newVersion;
    }

    private static def applyVersion(Project project, Version version) {
        project.android.applicationVariants.all { variant ->
            def versionCode = version.versionCode()
            def versionName = version.versionName()
            variant.mergedFlavor.versionCode = versionCode
            variant.mergedFlavor.versionName = versionName
        }
    }
}
