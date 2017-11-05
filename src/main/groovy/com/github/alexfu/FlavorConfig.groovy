package com.github.alexfu

class FlavorConfig {
    String releaseTask
    Closure[] postHooks = new Closure[0]

    def verify() {
        if (releaseTask == null) {
            throw new RuntimeException("releaseTask is undefined!")
        }
    }
}
