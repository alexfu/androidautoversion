package com.github.alexfu

import groovy.xml.Namespace
import groovy.xml.XmlUtil
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAutoVersionPlugin implements Plugin<Project> {
    private AndroidAutoVersionExtension extension
    private Version version

    @Override
    void apply(Project project) {
        extension = project.extensions.create("androidAutoVersion", AndroidAutoVersionExtension)
        setUp(project)
        applyVersion(project)
    }

    private void setUp(Project project) {
        File versionFile = project.file("version")
        version = new Version(versionFile)
        version.save()
    }

    private void applyVersion(Project project) {
        project.android.applicationVariants.all { variant ->
            variant.outputs.all { output ->
                output.processManifest.doLast {
                    File manifestFile = new File("$manifestOutputDirectory/AndroidManifest.xml")
                    Node manifest = new XmlParser().parse(manifestFile)
                    Namespace ns = new Namespace("http://schemas.android.com/apk/res/android", "android")

                    int versionCode = version.versionCode
                    String versionName = version.versionName
                    if (variant.buildType.versionNameSuffix) {
                        versionName += variant.buildType.versionNameSuffix
                    }

                    manifest.attributes().put(ns.versionCode, versionCode)
                    manifest.attributes().put(ns.versionName, versionName)
                    manifestFile.write(XmlUtil.serialize(manifest))
                }
            }
        }
    }
}
