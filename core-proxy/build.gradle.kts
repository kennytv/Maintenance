dependencies {
    api(projects.maintenanceApiProxy)
    api(projects.maintenanceCore)
    implementation(libs.hikariCP)
    compileOnly(libs.luckperms)
    compileOnly(libs.guava)
    compileOnly(libs.gson)
}

java {
    withJavadocJar()
}
