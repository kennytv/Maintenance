tasks {
    shadowJar {
        relocate("com.google.protobuf", "eu.kennytv.maintenance.lib.protobuf")
    }
}

dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(libs.bstatsVelocity)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
