dependencies {
    api(projects.maintenanceApiProxy)
    api(projects.maintenanceCore)

    // Try to reasonably minimize it...
    implementation(libs.jedis) {
        exclude("com.google.code.gson", "gson")
        exclude("org.slf4j", "slf4j-api")
    }

    compileOnly(libs.luckperms)
    compileOnly(libs.guava)
    compileOnly(libs.gson)
}

java {
    withJavadocJar()
}
