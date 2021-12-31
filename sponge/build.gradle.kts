dependencies {
    implementation(projects.maintenanceCore)
    implementation(projects.adventure.adventurePlatformSponge) {
        targetConfiguration = "shadow"
    }
    compileOnly(libs.sponge)
    annotationProcessor(libs.sponge)
}
