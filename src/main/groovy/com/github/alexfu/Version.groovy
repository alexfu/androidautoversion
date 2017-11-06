package com.github.alexfu

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Version {
    int buildNumber = 0
    int patch = 0
    int minor = 0
    int major = 0

    Version() {/* No op */}

    Version(File file) {
        def version = new JsonSlurper().parseText(file.text)
        buildNumber = version.buildNumber
        patch = version.patch
        minor = version.minor
        major = version.major
    }

    String getVersionName() {
        return "$major.$minor.$patch.$buildNumber"
    }

    int getVersionCode() {
        return buildNumber
    }

    String toJson() {
        return JsonOutput.toJson(major: major,
            minor: minor,
            patch: patch,
            buildNumber: buildNumber
        )
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
        return versionName
    }
}
