package com.github.alexfu

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private AndroidAutoVersionExtension extension
    private Version version
    private File versionFile

    @Override
    void apply(Project project) {
        extension = project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)
        versionFile = project.file("version")

        if (versionFile.exists()) {
            version = new Version(versionFile)
        } else {
            version = new Version()
            versionFile.write(version.toJson())
        }


    }
}
