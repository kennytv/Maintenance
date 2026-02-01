import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar

plugins {
    id("maintenance.base-conventions")
    id("com.gradleup.shadow")
}

tasks {
    named<Jar>("jar") {
        archiveClassifier.set("unshaded")
        from(project.rootProject.file("LICENSE.txt"))
    }
    val shadowJar = named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        configureRelocations()
    }
    named("build") {
        dependsOn(shadowJar)
    }
}

publishShadowJar()

fun ShadowJar.configureRelocations() {
    relocate("org.bstats", "eu.kennytv.maintenance.lib.bstats")

    // Proxy
    relocate("redis.client", "eu.kennytv.maintenance.lib.redis.client")
    relocate("org.json", "eu.kennytv.maintenance.lib.json")
    relocate("org.apache.commons.pool2", "eu.kennytv.maintenance.lib.apache.commons.pool2")
}