enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
    }
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
    plugins {
        id("net.kyori.blossom") version "2.1.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.9"
        id("com.gradleup.shadow") version "8.3.5"
    }
}

rootProject.name = "maintenance-parent"

includeBuild("build-logic")

include("adventure", "adventure:adventure-api",
    "adventure:adventure-platform-bukkit", "adventure:adventure-platform-bungee")

subproject("api")
subproject("api-proxy")
subproject("core")
subproject("core-proxy")
subproject("paper")
subproject("sponge")
subproject("bungee")
subproject("velocity")

setupSubproject("maintenance") {
    projectDir = file("universal")
}

fun subproject(name: String) {
    setupSubproject("maintenance-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
