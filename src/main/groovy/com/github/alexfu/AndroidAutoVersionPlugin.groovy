package com.github.alexfu

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import groovy.xml.Namespace
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private Version version
    private boolean isAppPlugin
    private boolean isLibraryPlugin

    @Override
    void apply(Project project) {
        isAppPlugin = project.plugins.withType(AppPlugin)
        isLibraryPlugin = project.plugins.withType(LibraryPlugin)
        if (!isAppPlugin && !isLibraryPlugin) {
            throw new IllegalStateException("'com.android.application' or 'com.android.library' plugin required.")
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
        if (isAppPlugin) {
            AppExtension android = project.android
            applyVersion(android.applicationVariants)
        } else if (isLibraryPlugin) {
            LibraryExtension android = project.android
            applyVersion(android.libraryVariants)
        }
    }

    private applyVersion(DefaultDomainObjectSet<BaseVariant> variants) {
        variants.all { variant ->
            int versionCode = version.versionCode
            String versionName = version.versionName

            variant.mergedFlavor.versionCode = versionCode
            variant.mergedFlavor.versionName = versionName

            variant.outputs.all { output ->
                output.processManifest.doLast {
                    File manifestFile = new File("$manifestOutputDirectory/AndroidManifest.xml")
                    Node manifest = new XmlParser().parse(manifestFile)
                    Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

                    manifest.attributes().put(ns.versionCode, versionCode)
                    manifest.attributes().put(ns.versionName, applyVariantVersionNameSuffix(variant, versionName))
                    manifestFile.write(XmlUtil.serialize(manifest))
                }
            }
        }
    }

    private static String applyVariantVersionNameSuffix(BaseVariant variant, String versionName) {
        if (variant.mergedFlavor.versionNameSuffix != null) {
            return versionName + variant.mergedFlavor.versionNameSuffix
        } else if (variant.buildType.versionNameSuffix != null) {
            return versionName + variant.buildType.versionNameSuffix
        } else {
            return versionName
        }
    }
}
