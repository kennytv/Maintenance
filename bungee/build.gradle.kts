dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(projects.adventure.adventurePlatformBungee) {
        targetConfiguration = "shadow"
    }
    implementation(libs.bstatsBungee)
    compileOnly(libs.bungee)
}
