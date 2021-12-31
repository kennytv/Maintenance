dependencies {
    implementation(projects.maintenanceCore)
    implementation(projects.adventure.adventurePlatformBukkit) {
        targetConfiguration = "shadow"
    }
    compileOnly(libs.paper)
    compileOnly(libs.authlib)
    compileOnly(libs.protocollib)
}