dependencies {
    api(projects.maintenanceApiProxy)
    api(projects.maintenanceCore)
    compileOnly(libs.luckperms)
    compileOnly(libs.lettuce)
    compileOnly(libs.guava)
    compileOnly(libs.gson)
}

java {
    withJavadocJar()
}
