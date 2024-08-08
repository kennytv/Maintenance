plugins {
    id("com.gradleup.shadow")
}

// Shade and relocate adventure in an extra module, so that common/the rest can directly depend on a
// relocated adventure without breaking native platform's adventure usage with project wide relocation
tasks {
    shadowJar {
        relocate("net.kyori", "eu.kennytv.maintenance.lib.kyori")
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    api(libs.bundles.adventure) {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
        exclude("com.google.code.gson", "gson")
    }
}

publishShadowJar()
