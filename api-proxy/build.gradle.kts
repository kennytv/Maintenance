plugins {
    id("maintenance.shadow-conventions")
}

dependencies {
    api(projects.maintenanceApi)
    compileOnly(libs.adventureApi)
}

java {
    withJavadocJar()
}
