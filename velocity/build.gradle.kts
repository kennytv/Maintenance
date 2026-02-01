dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(libs.bstatsVelocity)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
