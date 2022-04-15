import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME

plugins {
    base
    id("maintenance.build-logic")
}

allprojects {
    group = "eu.kennytv.maintenance"
    version = property("projectVersion") as String // from gradle.properties
    description = "Enable maintenance mode with a custom maintenance motd and icon."
}

val platforms = setOf(
    projects.maintenanceSpigot,
    projects.maintenanceBungee,
    projects.maintenanceSponge,
    projects.maintenanceVelocity
).map { it.dependencyProject }

val special = setOf(
    projects.maintenance,
    projects.maintenanceApi,
    projects.maintenanceApiProxy,
    projects.adventure,
    projects.adventure.adventureApi,
    projects.adventure.adventurePlatformBukkit,
    projects.adventure.adventurePlatformBungee,
).map { it.dependencyProject }

subprojects {
    when (this) {
        in platforms -> plugins.apply("maintenance.platform-conventions")
        in special -> plugins.apply("maintenance.base-conventions")
        else -> plugins.apply("maintenance.standard-conventions")
    }

    dependencies {
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.bundles.junit)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.snakeyaml)
    }
}