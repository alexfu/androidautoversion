# Android Auto-Version
This is a Gradle plugin, for Android developers, that automates app versioning. You can read more
about this plugins inception here: http://alexfu.github.io/2015/11/09/Android-Auto-Versioning

# Why
Not having to remember or worry about when to bump version numbers allows you to focus on what's
really important (implementing new features or fixing bugs).

# Benefits
This plugin comes with 3 simple tasks:

- `releaseMajor`
- `releaseMinor`
- `releasePatch`

All of these tasks will bump the version based on semantic versioning rules (http://semver.org)

# How
To use this plugin...

## Step 1
Include the following in your top-level `build.gradle` file:

```groovy
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.alexfu:androidautoversion:0.3.0'
  }
}
```

## Step 2
Include the following in your app-level `build.gradle` file:

```groovy
apply plugin: 'com.github.alexfu.androidautoversion'

androidAutoVersion {
  releaseTask = "assembleRelease"
}
```

## Step 3
Remove `versionCode` and `versionName` from your `defaultConfig` block!

# Usage
Every time you want to make a release, decide if it's a major, minor, or a patch. Use the rules
outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run the
release task that matches your release type (`releaseMajor`, `releaseMinor`, `releasePatch`).
At the end of it all, you'll have release APKs of each app variant.

## Beta
This plugin supports releasing betas. To release a beta, you must first specify a `betaReleaseTask`:

```groovy
androidAutoVersion {
  betaReleaseTask = "assembleRelease"
}
```

You now have access to the beta variants of the release tasks: `releaseBetaMajor`, `releaseBetaMinor`, and `releaseBetaPatch`. Whenever you run one of these tasks, the version name will have `-beta` appended to it.

## Version File
You will notice that this plugin creates a `version` file for you in the root level directory of your project. This is how the plugin tracks and updates the version. If you would like this file to live elsewhere, you can specify a custom file location like so:

```groovy
androidAutoVersion {
  versionFile = file("/path/to/version/file")
}
```

## Post Hooks
Sometimes you want to execute a script or run some command after the release task has completed. You can do this by specifying a list of `postHooks`:

```groovy
androidAutoVersion {
  postHooks = [ 
    { versionString -> 
      println("Hello $versionString"!) 
    } 
  ]
}
```

Post hooks are nothing more than closures with a single argument, the version string (version name). 
