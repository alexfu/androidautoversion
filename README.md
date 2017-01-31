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
    classpath 'com.github.alexfu:androidautoversion:0.2.1'
  }
}
```

## Step 2
Include the following in your app-level `build.gradle` file:

```groovy
apply plugin: 'com.github.alexfu.androidautoversion'

androidAutoVersion {
  releaseTask "assembleRelease"
}
```

## Step 3
Remove `versionCode` and `versionName` from your `defaultConfig` block!

# Usage
Every time you want to make a release, decide if it's a major, minor, or a patch. Use the rules
outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run the
release task that matches your release type (`releaseMajor`, `releaseMinor`, `releasePatch`).
At the end of it all, you'll have a tagged branch and release APKs of each app variant.

## Version File
You will notice that this plugin creates a `version` file for you in the root level directory of your project. This is how the plugin tracks and updates the version. If you would like this file to live elsewhere, you can specify a custom file location like so:

```groovy
androidAutoVersion {
  releaseTask "assembleRelease"
  versionFile file("/path/to/version/file")
}
```
