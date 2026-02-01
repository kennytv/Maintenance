import dev.lukebemish.centralportalpublishing.CentralPortalRepositoryHandlerExtension

plugins {
    `java-library`
    `maven-publish`
    signing
    id("dev.lukebemish.central-portal-publishing")
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
    publish {
        dependsOn(tasks.named("publishMaintenanceCentralPortalBundle"))
    }
}

java {
    javaTarget(21)
    withSourcesJar()
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

centralPortalPublishing.bundle("maintenance") {
    username = System.getenv("MAVEN_CENTRAL_USERNAME")
    password = System.getenv("MAVEN_CENTRAL_PASSWORD")
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

    if (!name.startsWith("adventure")) {
        repositories {
            val portal = (this as ExtensionAware).extensions.getByType(CentralPortalRepositoryHandlerExtension::class)
            portal.portalBundle(":", "maintenance")
        }
    }
}