import java.util.*

plugins {
    id("maintenance.shadow-conventions")
}

tasks {
    shadowJar {
        archiveFileName.set("Maintenance-${project.name.substringAfter("maintenance-").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}-${project.version}.jar")
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
    }
}
