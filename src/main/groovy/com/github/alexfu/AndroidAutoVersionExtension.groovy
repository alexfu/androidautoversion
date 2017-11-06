package com.github.alexfu

import org.gradle.api.Action
import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile = new File("version")
    Closure<String> versionFormatter = { int major, int minor, int patch, int buildNumber ->
        return "${major}.${minor}.${patch}"
    }
    Closure[] postHooks = new Closure[0]
    @Nullable FlavorConfig releaseConfig
    @Nullable FlavorConfig betaConfig

    def release(Action<FlavorConfig> action) {
        if (releaseConfig == null) {
            releaseConfig = new FlavorConfig()
        }
        action.execute(releaseConfig)
    }

    def beta(Action<FlavorConfig> action) {
        if (betaConfig == null) {
            betaConfig = new FlavorConfig()
        }
        action.execute(betaConfig)
    }
}
