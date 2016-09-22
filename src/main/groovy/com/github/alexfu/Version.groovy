package com.github.alexfu

import groovy.json.internal.LazyMap

import static com.github.alexfu.AndroidAutoVersionPlugin.VersionType.MAJOR
import static com.github.alexfu.AndroidAutoVersionPlugin.VersionType.MINOR
import static com.github.alexfu.AndroidAutoVersionPlugin.VersionType.PATCH

class Version {
    int buildNumber = 0;
    int patch = 0;
    int minor = 0;
    int major = 0;
    int revision = 0;
    Closure<String> formatter;

    Version(Version source) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
        revision = source.revision;
        formatter = source.formatter;
    }

    Version(LazyMap source, Closure<String> formatter) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
        revision = source.revision;
        this.formatter = formatter;
    }

    String versionName() {
        if (formatter == null) {
            return applyRevision("${major}.${minor}.${patch}")
        }
        return applyRevision(formatter.call(major, minor, patch, buildNumber))
    }

    String betaVersionName() {
        return applyRevision("$versionName()-beta")
    }

    int versionCode() {
        return buildNumber;
    }

    String toJson() {
        return "{\"major\": $major, \"minor\": $minor, \"patch\": $patch, \"buildNumber\": $buildNumber}"
    }

    void update(AndroidAutoVersionPlugin.VersionType type) {
        buildNumber += 1
        switch (type) {
            case MAJOR:
                revision = 0
                patch = 0
                minor = 0
                major += 1
                break;
            case MINOR:
                revision = 0
                patch = 0
                minor += 1
                break;
            case PATCH:
                revision = 0
                patch += 1
                break;
        }
    }

    @Override
    String toString() {
        return versionName();
    }

    private String applyRevision(String name) {
        if (revision == 0) return name
        return "$name.$revision"
    }
}
