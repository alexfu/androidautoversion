# Android Auto-Version
This is a Gradle plugin, for Android developers, that automates app versioning.

# Why
Thinking of versioning in terms of major, minor, and patch makes it easier to update your app and takes the guess work out of it.

Issue one of these gradle commands:

- `./gradlew bumpPatch`
- `./gradlew bumpMinor`
- `./gradlew bumpMajor`

and your app will be updated accordingly.

# Installation

## Step 1
Include the following in your top-level `build.gradle` file:

```groovy
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.alexfu:androidautoversion:feature~v2-SNAPSHOT'
  }
}
```

## Step 2
Include the following in your app-level `build.gradle` file:

```groovy
apply plugin: 'com.github.alexfu.androidautoversion'
```

## Step 3
Remove `versionCode` and `versionName` from your `defaultConfig` block!

# Usage
Every time you want to make a release, decide if it's a major, minor, or a patch. If you're not sure, check out the rules outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run one of the following gradle tasks:

- `./gradlew bumpPatch`
- `./gradlew bumpMinor`
- `./gradlew bumpMajor`

Running one of these will update the version but will not make a release. To update and make a release, you can append your release task to the end of your update task. For example:

```bash
./gradlew bumpMinor assembleRelease
```
