package com.github.alexfu

import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private enum VersionType {
        MAJOR, MINOR, PATCH
    }

    @Override
    void apply(Project project) {
        project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)

        project.afterEvaluate {
            applyVersion(project, project.androidAutoVersion.getVersion())
        }

        // Create prepare tasks
        def majorPrepTask = makePrepareTask(project, VersionType.MAJOR);
        def minorPrepTask = makePrepareTask(project, VersionType.MINOR);
        def patchPrepTask = makePrepareTask(project, VersionType.PATCH);

        // Set up task ordering
        project.tasks.whenTaskAdded { task ->
            if (task.name.equals("assembleRelease")) {
                task.mustRunAfter(majorPrepTask, minorPrepTask, patchPrepTask)
            }
        }

        // Create release tasks
        makeReleaseTask(project, VersionType.MAJOR, majorPrepTask)
        makeReleaseTask(project, VersionType.MINOR, minorPrepTask)
        makeReleaseTask(project, VersionType.PATCH, patchPrepTask)
    }

    private def makeReleaseTask(Project project, VersionType type, Task prepTask) {
        def name;
        switch (type) {
            case VersionType.MAJOR:
                name = "releaseMajor";
                break;
            case VersionType.MINOR:
                name = "releaseMinor";
                break;
            case VersionType.PATCH:
                name = "releasePatch";
                break;
        }

        return project.task(name, {
            dependsOn prepTask.name, "assembleRelease"
        })
    }

    private def makePrepareTask(Project project, VersionType type) {
        def name;
        switch (type) {
            case VersionType.MAJOR:
                name = "prepareMajorRelease";
                break;
            case VersionType.MINOR:
                name = "prepareMinorRelease";
                break;
            case VersionType.PATCH:
                name = "preparePatchRelease";
                break;
        }

        return project.task(name) << {
            def extension = (AndroidAutoVersionExtension) project.androidAutoVersion;
            def version = extension.getVersion();
            def newVersion = updateVersion(version, type);

            // Save new version
            extension.saveVersion(newVersion)

            // Apply to all variants
            applyVersion(project, newVersion)

            def git = Grgit.open(project.rootDir)

            // Add changes
            git.add(update: true, patterns: ["config/version"])

            // Commit
            git.commit(message: "Update version")

            // Tag
            git.tag.add(name: "${newVersion.toString()}")
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
