import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

plugins {
    id("maintenance.base-conventions")
    id("com.github.johnrengelman.shadow")
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
    relocate("com.zaxxer.hikari", "eu.kennytv.maintenance.lib.hikari")
    relocate("org.bstats", "eu.kennytv.maintenance.lib.bstats")
}