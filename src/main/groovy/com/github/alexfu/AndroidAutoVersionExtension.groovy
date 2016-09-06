package com.github.alexfu

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap

class AndroidAutoVersionExtension {
    File versionFile
    Closure<String> versionFormatter
    private Version version

    def getVersion() {
        sanityCheck();
        return version;
    }

    def saveVersion(Version version) {
        this.version = version;

        def contents = new JsonBuilder(version).toPrettyString();
        versionFile.write(contents);
    }

    private def sanityCheck() {
        if (!version) {
            LazyMap map = new JsonSlurper().parseText(versionFile.text);
            version = new Version(map, versionFormatter)
        }
    }
}
