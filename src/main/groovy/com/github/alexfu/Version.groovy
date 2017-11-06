package com.github.alexfu

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class Version {
    private int buildNumber = 1
    private int patch = 0
    private int minor = 0
    private int major = 0
    private File file

    Version(File file) {
        if (file.exists()) {
            def json = new JsonSlurper().parseText(file.text)
            buildNumber = json.buildNumber
            patch = json.patch
            minor = json.minor
            major = json.major
        }
        this.file = file
    }

    String getVersionName() {
        return "$major.$minor.$patch"
    }

    int getVersionCode() {
        return buildNumber
    }

    void update(VersionType type) {
        buildNumber += 1
        switch (type) {
            case VersionType.MAJOR:
                patch = 0
                minor = 0
                major += 1
                break
            case VersionType.MINOR:
                patch = 0
                minor += 1
                break
            case VersionType.PATCH:
                patch += 1
                break
        }
        save()
    }

    @Override
    String toString() {
        return versionName
    }

    void save() {
        file.write(toJson())
    }

    private String toJson() {
        return JsonOutput.toJson(major: major,
            minor: minor,
            patch: patch,
            buildNumber: buildNumber
        )
    }
}
