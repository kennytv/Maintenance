plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.gradleup.nmcp")
}

tasks {
    // Variable replacements
    processResources {
        val ver = project.version.toString()
        val desc = project.description
        filesMatching(listOf("plugin.yml", "bungee.yml", "META-INF/sponge_plugins.json")) {
            expand(mapOf("version" to ver, "description" to desc))
        }
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-unchecked", "-Xlint:-deprecation"))
    }
    test {
        useJUnitPlatform()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
    withJavadocJar()
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = project.name
        version = rootProject.version as String
        pom {
            name.set("Maintenance")
            description.set("Paper plugin to enable maintenance mode on your Minecraft server.")
            url.set("https://github.com/kennytv/Maintenance")
            scm {
                url.set("https://github.com/kennytv/Maintenance")
                connection.set("scm:git:https://github.com/kennytv/Maintenance.git")
                developerConnection.set("scm:git:ssh://git@github.com/kennytv/Maintenance.git")
            }
            licenses {
                license {
                    name.set("GNU GPLv3")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                }
            }
            developers {
                developer {
                    id.set("kennytv")
                    name.set("Nassim Jahnke")
                }
            }
        }
    }
}
