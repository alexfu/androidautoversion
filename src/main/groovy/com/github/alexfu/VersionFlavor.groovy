package com.github.alexfu

enum VersionFlavor {
    RELEASE("Release"), BETA("Beta")

    private final String name

    private VersionFlavor(String name) {
        this.name = name
    }
}
