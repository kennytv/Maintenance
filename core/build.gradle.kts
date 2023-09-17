plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
                property("impl_version", "git-Maintenance-${project.version}:${rootProject.latestCommitHash()}")
            }
        }
    }
}

dependencies {
    api(projects.maintenanceApi)
    api(projects.adventure.adventureApi) {
        targetConfiguration = "shadow"
    }
    compileOnly(libs.luckperms)
    compileOnly(libs.serverlistplus)
    compileOnly(libs.guava)
    compileOnly(libs.gson)
    compileOnly(libs.snakeyaml)
}

java {
    withJavadocJar()
}
