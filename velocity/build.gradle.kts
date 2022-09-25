tasks {
    shadowJar {
        relocate("com.google.protobuf", "eu.kennytv.maintenance.lib.protobuf")
        relocate("com.mysql", "eu.kennytv.maintenance.lib.mysql")
    }
}

dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(libs.mysqlConnector)
    implementation(libs.bstatsVelocity)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
