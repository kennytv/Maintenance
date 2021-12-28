plugins {
    id("com.github.johnrengelman.shadow")
}

// Shade and relocate adventure in an extra module, so that common/the rest can directly depend on a
// relocated adventure without breaking native platform's adventure usage with project wide relocation
tasks {
    shadowJar {
        relocate("net.kyori", "eu.kennytv.maintenance.lib.kyori")
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    api("net.kyori:adventure-api:4.10.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-bom")
        exclude("com.google.code.gson", "gson")
    }
    api("net.kyori:adventure-text-minimessage:4.10.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
    }
    api("net.kyori:adventure-text-serializer-legacy:4.10.0-SNAPSHOT") {
        exclude("net.kyori", "adventure-api")
        exclude("net.kyori", "adventure-bom")
    }
    //TODO move into extra modules :>
    api("net.kyori:adventure-platform-bukkit:4.0.1") {
        exclude("net.kyori", "adventure-api")
    }
    api("net.kyori:adventure-platform-bungeecord:4.0.1") {
        exclude("net.kyori", "adventure-api")
    }
    api("net.kyori:adventure-platform-spongeapi:4.0.1") {
        exclude("net.kyori", "adventure-api")
    }
}

publishShadowJar()
