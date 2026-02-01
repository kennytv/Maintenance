dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(libs.bstatsBungee)
    compileOnly(libs.bungee)
}

tasks {
    shadowJar {
        relocate("net.kyori", "eu.kennytv.maintenance.lib.kyori") {
            exclude("net.kyori", "adventure-bom")
            exclude("com.google.code.gson", "gson")
        }
    }
}

dependencies {
    api(libs.bundles.adventureBungee) {
        exclude("com.google.code.gson", "gson")
        exclude("org.slf4j", "slf4j-api")
        exclude("net.kyori", "adventure-bom")
    }
}
