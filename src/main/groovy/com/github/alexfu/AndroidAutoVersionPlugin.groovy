package com.github.alexfu

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    enum VersionType {
        MAJOR("Major"), MINOR("Minor"), PATCH("Patch"), NONE("")
        static def all() {
            return [MAJOR, MINOR, PATCH, NONE]
        }

        private final String name;

        private VersionType(String name) {
            this.name = name;
        }
    }

    enum VersionFlavor {
        RELEASE("Release"), BETA("Beta")

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

    private def makeReleaseTasks(Project project) {
        def types = VersionType.all()
        def flavors = [VersionFlavor.RELEASE]

        if (extension.betaReleaseTask != null) {
            flavors.add(VersionFlavor.BETA)
        }

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
            def releaseTask = extension.releaseTask
            dependencies.add(releaseTask)
            def task = project.getTasks().getByName(releaseTask)
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
            version.update(type);

            // Save new version
            extension.saveVersion(version)

            // Apply to all variants
            applyVersion(project, flavor)

            def git = Grgit.open(project.rootDir)

            // Add changes
            def currentDir = new File(new File("").absolutePath) // File must use absolute path
            def versionFilePath = currentDir.toPath().relativize(extension.versionFile.toPath()).toString()
            git.add(update: true, patterns: [versionFilePath])

            // Commit
            git.commit(message: "Update version")

            // Tag
            git.tag.add(name: "${version.versionNameForFlavor(flavor)}")
        }
    }

    private def applyVersion(Project project, VersionFlavor flavor = VersionFlavor.RELEASE) {
        project.android.applicationVariants.all { variant ->
            def versionCode = extension.getVersion().versionCode()
            def versionName = extension.getVersion().versionNameForFlavor(flavor)
            variant.mergedFlavor.versionCode = versionCode
            variant.mergedFlavor.versionName = versionName
        }
    }
}
