package com.github.alexfu

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Version {
    int buildNumber = 0
    int patch = 0
    int minor = 0
    int major = 0
    int revision = 0
    final Closure<String> formatter

    Version(Closure<String> formatter) {
        this.formatter = formatter
    }

    Version(File file, Closure<String> formatter) {
        def version = new JsonSlurper().parseText(file.text)
        buildNumber = version.buildNumber
        patch = version.patch
        minor = version.minor
        major = version.major
        if (version.revision) {
            revision = Math.max(0, version.revision)
        }
        this.formatter = formatter
    }

    private String versionName() {
        return formatter.call(major, minor, patch, buildNumber)
    }

    String versionNameForFlavor(VersionFlavor flavor) {
        if (flavor == VersionFlavor.BETA) {
            return betaVersionName()
        }
        return releaseVersionName()
    }

    private String releaseVersionName() {
        return versionName()
    }

    private String betaVersionName() {
        return applyRevision("${versionName()}-beta")
    }

    int versionCode() {
        return buildNumber
    }

    String toJson() {
        return JsonOutput.toJson([major: major,
                                  minor: minor,
                                  patch: patch,
                                  revision: revision,
                                  buildNumber: buildNumber])
    }

    void update(VersionType type) {
        buildNumber += 1
        revision += 1
        switch (type) {
            case VersionType.MAJOR:
                revision = 0
                patch = 0
                minor = 0
                major += 1
                break
            case VersionType.MINOR:
                revision = 0
                patch = 0
                minor += 1
                break
            case VersionType.PATCH:
                revision = 0
                patch += 1
                break
        }
    }

    @Override
    String toString() {
        return versionName()
    }

    private String applyRevision(String name) {
        if (revision < 2) return name
        return "$name.${revision-1}"
    }
}
