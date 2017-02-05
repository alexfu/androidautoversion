package com.github.alexfu

import org.gradle.api.Action
import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile = new File("version")
    Closure<String> versionFormatter = { int major, int minor, int patch, int buildNumber ->
        return "${major}.${minor}.${patch}"
    }
    Closure[] postHooks = new Closure[0]
    private final FlavorConfig releaseConfig = new FlavorConfig()
    @Nullable private FlavorConfig betaConfig

    def release(Action<FlavorConfig> action) {
        action.execute(releaseConfig)
    }

    def beta(Action<FlavorConfig> action) {
        if (betaConfig == null) {
            betaConfig = new FlavorConfig()
        }
        action.execute(betaConfig)
    }

    FlavorConfig releaseConfig() {
        return releaseConfig
    }

    @Nullable FlavorConfig betaConfig() {
        return betaConfig
    }
}
