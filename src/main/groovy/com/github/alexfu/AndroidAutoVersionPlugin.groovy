package com.github.alexfu

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private AndroidAutoVersionExtension extension
    private Version version

    @Override
    void apply(Project project) {
        extension = project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)
        setUp(project)
        applyVersion(project)
    }

    private void setUp(Project project) {
        File versionFile = project.file("version")
        version = new Version(versionFile)
        version.save()
    }

    private void applyVersion(Project project) {
        project.android.defaultConfig.versionCode = version.versionCode
        project.android.defaultConfig.versionName = version.versionName
    }
}
