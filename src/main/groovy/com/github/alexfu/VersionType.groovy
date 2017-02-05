package com.github.alexfu

enum VersionType {
    MAJOR("Major"), MINOR("Minor"), PATCH("Patch"), NONE("")

    static all() {
        return [MAJOR, MINOR, PATCH, NONE]
    }

    final String name

    private VersionType(String name) {
        this.name = name
    }
}
