package com.github.alexfu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import groovy.xml.Namespace
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private Version version

    @Override
    void apply(Project project) {
        boolean hasAndroidAppPlugin = project.plugins.withType(AppPlugin)
        if (!hasAndroidAppPlugin) {
            throw new IllegalStateException("'com.android.application' plugin required.")
        }

        setUp(project)
        applyVersion(project)
        createTasks(project)
    }

    private void createTasks(Project project) {
        project.task("bumpPatch").doLast {
            version.update(VersionType.PATCH)
            applyVersion(project)
        }

        project.task("bumpMinor").doLast {
            version.update(VersionType.MINOR)
            applyVersion(project)
        }

        project.task("bumpMajor").doLast {
            version.update(VersionType.MAJOR)
            applyVersion(project)
        }
    }

    private void setUp(Project project) {
        File versionFile = project.file("version")
        version = new Version(versionFile)
    }

    private void applyVersion(Project project) {
        AppExtension android = project.android
        android.applicationVariants.all { variant ->
            variant.outputs.all { output ->
                output.processManifest.doLast {
                    File manifestFile = new File("$manifestOutputDirectory/AndroidManifest.xml")
                    Node manifest = new XmlParser().parse(manifestFile)
                    Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

                    int versionCode = version.versionCode
                    String versionName = applyVariantVersionNameSuffix(variant, version.versionName)

                    manifest.attributes().put(ns.versionCode, versionCode)
                    manifest.attributes().put(ns.versionName, versionName)
                    manifestFile.write(XmlUtil.serialize(manifest))
                }
            }
        }
    }

    private static String applyVariantVersionNameSuffix(ApplicationVariant variant, String versionName) {
        if (variant.mergedFlavor.versionNameSuffix != null) {
            return versionName + variant.mergedFlavor.versionNameSuffix
        } else if (variant.buildType.versionNameSuffix != null) {
            return versionName + variant.buildType.versionNameSuffix
        } else {
            return versionName
        }
    }
}
