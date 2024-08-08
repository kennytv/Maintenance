plugins {
    id("com.gradleup.shadow")
}

tasks {
    shadowJar {
        relocate("net.kyori", "eu.kennytv.maintenance.lib.kyori")
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    api(libs.adventurePlatformBungee) {
        exclude("net.kyori", "adventure-api")
    }
}

publishShadowJar()
