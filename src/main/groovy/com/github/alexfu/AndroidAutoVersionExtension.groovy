package com.github.alexfu

import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile
    String releaseTask

    @Nullable Closure<String> versionFormatter
    @Nullable String betaReleaseTask

    private Version version

    def getVersion() {
        sanityCheck()
        return version
    }

    def saveVersion(Version version) {
        this.version = version
        versionFile.write(version.toJson())
    }

    private sanityCheck() {
        if (!version) {
            LazyMap map = new JsonSlurper().parseText(versionFile.text)
            version = new Version(map, versionFormatter)
        }
    }
}
