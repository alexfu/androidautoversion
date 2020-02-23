# AndroidAutoVersion
This is a Gradle plugin, for Android developers, that automates app versioning.

# Features

- Update app version from CLI
- Automatically commit version update to git 
- Automatically creates git tag

```
AndroidAutoVersion tasks
------------------------
bumpMajor - Increases major version by 1, zeroes out minor and patch version
bumpMinor - Increases minor version by 1 and zeroes out patch version
bumpPatch - Increases patch version by 1
versionMajor - Executes bumpMajor and commits the changes to git
versionMinor - Executes bumpMinor and commits the changes to git
versionPatch - Executes bumpPatch and commits the changes to git
```

# Requirements

- Android Gradle Plugin 3.0+
- Gradle 4.0+
- Git

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
Replace `versionCode` and `versionName`:

```groovy
  defaultConfig {
    versionName androidAutoVersion.versionName
    versionCode androidAutoVersion.versionCode
  }
```

# Usage
When building your project for the first time with this plugin, you should notice a new file added to your project: `[module name]/version`. This is called a version file. You should check this file into version control (i.e. git) since this file will contain the current version information.

If you're adding AndroidAutoVersion to an already existing project you need to populate the version file's `major`, `minor`, `patch`, and `buildNumber` fields with what was in the `versionName` and `versionCode` fields.

For example, a project with the following `app/build.gradle` file:

```groovy
  defaultConfig {
    versionName "2.0.5"
    versionCode 9
  }
```

Should update the version file to look like so:

```json
{"major":2,"minor":0,"patch":5,"buildNumber":9}
```

Every time you want to make a release, decide if it's a major, minor, or a patch. If you're not sure, check out the rules outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run one of the following gradle tasks:

- `./gradlew bumpPatch`
- `./gradlew bumpMinor`
- `./gradlew bumpMajor`

Running one of these will update the version, but it will be up to you to commit any changes to version control. 

If you use `git` (which you should), the plugin also has tasks that will execute the corresponding bump task, commit the version update, and create the necessary version tag.

- `./gradlew versionPatch`
- `./gradlew versionMinor`
- `./gradlew versionMajor`

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
$ ./gradlew clean versionPatch assembleRelease
```

# Migrating from v1
If you're updating this plugin from version 1, read the [migration guide](https://github.com/alexfu/androidautoversion/wiki/Migration-Guide).
