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

All of these tasks will do the following when executed:

1. Bump version based on semantic versioning rules (http://semver.org)
2. Commit the versioning file (using `git`)
3. Tag your current branch (again, using `git`)

**NOTE:** This plugin will *not* push any changes to your remote server.
This is up to the developer to do.

# Caveats
There is one caveat. Since this plugin uses a system prompt in order to ensure the user really wants
to make a release, it has been known that `System.console()` will return null if a gradle daemon is
running. To work around this, run the desired release task with the `--no-daemon` flag (i.e `./gradlew releaseMajor --no-daemon`).
There is currently an open issue for this [here](https://issues.gradle.org/browse/GRADLE-2310).

# How
To use this plugin...

## Step 1
Create a versioning file. This file is a simple JSON file that specifies the components that make
up your version. Include it in your VCS.

```javascript
{
  "buildNumber": 99,
  "major": 1,
  "minor": 0,
  "patch": 1
}
```

## Step 2
Include the following in your top-level `build.gradle` file:

```groovy
buildscript {
  repositories {
    maven { url 'https://jitpack.io' }
  }
  dependencies {
    classpath 'com.github.alexfu:androidautoversion:0.1.1'
  }
}
```

## Step 3
Include the following in your app-level `build.gradle` file:

```groovy
apply plugin 'com.github.alexfu.androidautoversion'

androidAutoVersion {
  versionFile file('/path/to/version/file')
}
```
## Step 4
Remove `versionCode` and `versionName` from your `defaultConfig` block!

# Usage
Every time you want to make a release, decide if it's a major, minor, or a patch. Use the rules
outlined [here](http://semver.org/) to make your decision. Then, once you've decided, run the
release task that matches your release type (`releaseMajor`, `releaseMinor`, `releasePatch`).
At the end of it all, you'll have a tagged branch and release APKs of each app variant.
