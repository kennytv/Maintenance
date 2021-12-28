dependencies {
    api(projects.maintenanceApiProxy)
    api(projects.maintenanceCore)
    implementation("com.zaxxer:HikariCP:4.0.3")
    compileOnly("net.luckperms:api:5.3")
    compileOnly("com.google.guava:guava:29.0-jre")
    compileOnly("com.google.code.gson:gson:2.8.5")
}

java {
    withJavadocJar()
}
