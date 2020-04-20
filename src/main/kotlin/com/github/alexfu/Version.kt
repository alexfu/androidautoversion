package com.github.alexfu

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val buildNumber: Int = 1,
    val patch: Int = 0,
    val minor: Int = 0,
    val major: Int = 0
) {
    val versionName: String
        get() = "$major.$minor.$patch"

    fun increment(type: VersionType): Version {
        return when (type) {
            VersionType.MAJOR -> {
                copy(buildNumber = buildNumber + 1, patch = 0, minor = 0, major = major + 1)
            }
            VersionType.MINOR -> {
                copy(buildNumber = buildNumber + 1, patch = 0, minor = minor + 1)
            }
            VersionType.PATCH -> {
                copy(buildNumber = buildNumber + 1, patch = patch + 1)
            }
        }
    }

    override fun toString(): String {
        return versionName
    }
}
