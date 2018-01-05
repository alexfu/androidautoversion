# AndroidAutoVersion
This is a Gradle plugin, for Android developers, that automates app versioning.

# Migrating from v1
If you're updating this plugin from version 1, read the [migration guide](https://github.com/alexfu/androidautoversion/wiki/Migration-Guide).

# Why
Thinking of versioning in terms of major, minor, and patch makes it easier to update your app and takes the guess work out of it.

Issue one of these gradle commands:

- `./gradlew bumpPatch`
- `./gradlew bumpMinor`
- `./gradlew bumpMajor`

and your app will be updated accordingly.

# Requirements

- Android Gradle Plugin 3.0+
- Gradle 4.0+

# Installation

## Step 1
Include the following in your top-level `build.gradle` file:

```groovy
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.alexfu:androidautoversion:$latest_version'
  }
}
```

Change `$latest_version` to the latest release version found [here](https://github.com/alexfu/androidautoversion/releases).

## Step 2
Include the following in your app-level `build.gradle` file:

```groovy
apply plugin: 'com.github.alexfu.androidautoversion'
```

## Step 3
Remove `versionCode` and `versionName` from your `defaultConfig` block!

# Usage
When building your project for the first time with this plugin, you should notice a new file added to your project: `[module name]/version`. This is called a verion file. You should check this file into version control (i.e. git) since this file will contain the current version information.

Every time you want to make a release, decide if it's a major, minor, or a patch. If you're not sure, check out the rules outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run one of the following gradle tasks:

- `./gradlew bumpPatch`
- `./gradlew bumpMinor`
- `./gradlew bumpMajor`

Running one of these will update the version but will not make a release. To update and make a release, you can append your release task to the end of your update task. For example:

```bash
./gradlew bumpMinor assembleRelease
```

# Tips

## Alpha/Beta
If you have alpha/beta versions of your app and want to signify that in your version, i.e. `1.2.3.alpha`, then you can use the `versionNameSuffix` property in your alpha/beta product flavors. For example:

```gradle
android {
    productFlavors {
        alpha {
            versionNameSuffix ".alpha"
        }

        beta {
            versionNameSuffix ".beta"
        }
    }
}
```

## Automate release workflow
Because this plugin allows you to update your app version from the command line, you can completely automate your entire release workflow with a simple script.

```bash
./gradlew clean bumpPatch assembleRelease && git add app/version && git commit -m "Update version."
```
