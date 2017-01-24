package com.github.alexfu

import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile
    String releaseTask
    Closure<String> versionFormatter = { int major, int minor, int patch, int buildNumber ->
        return "${major}.${minor}.${patch}"
    }
    @Nullable String betaReleaseTask
}
