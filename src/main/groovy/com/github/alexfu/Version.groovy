package com.github.alexfu

import groovy.json.internal.LazyMap

class Version {
    int buildNumber = 0;
    int patch = 0;
    int minor = 0;
    int major = 0;

    Version(Version source) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
    }

    Version(LazyMap source) {
        buildNumber = source.buildNumber;
        patch = source.patch;
        minor = source.minor;
        major = source.major;
    }

    String versionName() {
        return "${major}.${minor}.${patch}";
    }

    int versionCode() {
        return buildNumber;
    }

    @Override
    String toString() {
        return "${major}.${minor}.${patch}.${buildNumber}";
    }
}
