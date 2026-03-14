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
        id("net.kyori.blossom") version "2.2.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
        id("com.gradleup.shadow") version "9.3.1"
        id("dev.lukebemish.central-portal-publishing") version "0.1.7"
    }
}

rootProject.name = "maintenance-parent"

includeBuild("build-logic")

subproject("api")
subproject("api-proxy")
subproject("core")
subproject("core-proxy")
subproject("paper")
subproject("sponge")
subproject("bungee")
subproject("velocity")

fun subproject(name: String) {
    setupSubproject("maintenance-$name") {
        projectDir = file(name)
    }
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
    include(name)
    project(":$name").apply(block)
}
