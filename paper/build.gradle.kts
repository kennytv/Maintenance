dependencies {
    implementation(projects.maintenanceCore)
    implementation(projects.adventure.adventurePlatformBukkit) {
        targetConfiguration = "shadow"
    }
    implementation(libs.bstatsBukkit)
    compileOnly(libs.paper)
    compileOnly(libs.authlib)
    compileOnly(libs.protocollib)
}