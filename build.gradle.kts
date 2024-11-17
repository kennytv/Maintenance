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
    projects.maintenancePaper,
    projects.maintenanceBungee,
    projects.maintenanceSponge,
    projects.maintenanceVelocity
).map { it.path }

val special = setOf(
    projects.maintenance,
    projects.maintenanceApi,
    projects.maintenanceApiProxy,
    projects.adventure,
    projects.adventure.adventureApi,
    projects.adventure.adventurePlatformBukkit,
    projects.adventure.adventurePlatformBungee,
).map { it.path }

subprojects {
    when (path) {
        in platforms -> plugins.apply("maintenance.platform-conventions")
        in special -> plugins.apply("maintenance.base-conventions")
        else -> plugins.apply("maintenance.standard-conventions")
    }

    dependencies {
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.bundles.junit)
        TEST_IMPLEMENTATION_CONFIGURATION_NAME(rootProject.libs.snakeyaml)
    }
}