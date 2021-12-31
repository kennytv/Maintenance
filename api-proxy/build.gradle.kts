plugins {
    id("maintenance.shadow-conventions")
}

dependencies {
    api(projects.maintenanceApi)
    compileOnly(projects.adventure.adventureApi) {
        targetConfiguration = "shadow"
    }
}

java {
    withJavadocJar()
}
