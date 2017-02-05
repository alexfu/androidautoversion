package com.github.alexfu

import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile = new File("version")
    String releaseTask
    @Nullable String betaReleaseTask
    Closure<String> versionFormatter = { int major, int minor, int patch, int buildNumber ->
        return "${major}.${minor}.${patch}"
    }
    Closure[] postHooks = new Closure[0]
    Closure[] betaPostHooks = new Closure[0]
}
