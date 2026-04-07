import org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME
import org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME

plugins {
    base
    id("maintenance.build-logic")
    id("com.gradleup.nmcp.aggregation") version "1.4.4"
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
    projects.maintenanceApi,
    projects.maintenanceApiProxy
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
        TEST_RUNTIME_ONLY_CONFIGURATION_NAME("org.junit.platform:junit-platform-launcher")
    }
}

dependencies {
    allprojects {
        nmcpAggregation(project(path))
    }
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("MAVEN_CENTRAL_USERNAME")
        password = System.getenv("MAVEN_CENTRAL_PASSWORD")
        publishingType = "USER_MANAGED"
        publicationName = "maintenance:$version"
        publishingTimeout = java.time.Duration.ZERO
    }
}
