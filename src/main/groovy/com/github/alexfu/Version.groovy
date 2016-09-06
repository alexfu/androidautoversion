package com.github.alexfu

import groovy.json.internal.LazyMap

class Version {
    int buildNumber = 0;
    int patch = 0;
    int minor = 0;
    int major = 0;
    Closure<String> formatter;

    Version(Version source) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
        formatter = source.formatter;
    }

    Version(LazyMap source, Closure<String> formatter) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
        this.formatter = formatter;
    }

    String versionName() {
        if (formatter == null) {
            return "${major}.${minor}.${patch}"
        }
        return formatter.call(major, minor, patch, buildNumber)
    }

    String betaVersionName() {
        return "$versionName()-beta"
    }

    int versionCode() {
        return buildNumber;
    }

    String toJson() {
        return "{\"major\": $major, \"minor\": $minor, \"patch\": $patch, \"buildNumber\": $buildNumber}"
    }

    @Override
    String toString() {
        return versionName();
    }
}
