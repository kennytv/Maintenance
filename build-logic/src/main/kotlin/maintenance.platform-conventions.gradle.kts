plugins {
    id("maintenance.shadow-conventions")
}

tasks {
    shadowJar {
        archiveFileName.set("Maintenance-${project.name.substringAfter("maintenance-").capitalize()}-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}
