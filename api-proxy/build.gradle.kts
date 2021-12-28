plugins {
    id("maintenance.shadow-conventions")
}

dependencies {
    api(projects.maintenanceApi)
    compileOnly(projects.adventure) {
        targetConfiguration = "shadow"
    }
}

java {
    withJavadocJar()
}
