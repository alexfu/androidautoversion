package com.github.alexfu

import org.gradle.api.Action
import org.gradle.api.Nullable

class AndroidAutoVersionExtension {
    File versionFile = new File("version")
    String releaseTask
    Closure<String> versionFormatter = { int major, int minor, int patch, int buildNumber ->
        return "${major}.${minor}.${patch}"
    }
    Closure[] postHooks = new Closure[0]
    @Nullable private FlavorConfig betaConfig

    def beta(Action<FlavorConfig> action) {
        if (betaConfig == null) {
            betaConfig = new FlavorConfig()
        }
        action.execute(betaConfig)
    }

    @Nullable FlavorConfig betaConfig() {
        return betaConfig
    }
}
