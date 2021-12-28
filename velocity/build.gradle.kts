tasks {
    shadowJar {
        relocate("com.google.protobuf", "eu.kennytv.maintenance.lib.protobuf")
        relocate("com.mysql", "eu.kennytv.maintenance.lib.mysql")
    }
}

dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation("mysql:mysql-connector-java:8.0.27")
    compileOnly("com.velocitypowered:velocity-api:3.0.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.0.0-SNAPSHOT")
}
