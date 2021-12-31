dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(projects.adventure.adventurePlatformBungee) {
        targetConfiguration = "shadow"
    }
    compileOnly(libs.bungee)
}
