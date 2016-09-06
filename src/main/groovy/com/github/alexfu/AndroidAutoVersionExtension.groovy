package com.github.alexfu

import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap

class AndroidAutoVersionExtension {
    File versionFile
    Closure<String> versionFormatter
    String betaReleaseTask
    private Version version

    def getVersion() {
        sanityCheck();
        return version;
    }

    def saveVersion(Version version) {
        this.version = version;
        versionFile.write(version.toJson());
    }

    private def sanityCheck() {
        if (!version) {
            LazyMap map = new JsonSlurper().parseText(versionFile.text);
            version = new Version(map, versionFormatter)
        }
    }
}
