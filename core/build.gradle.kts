plugins {
    id("net.kyori.blossom")
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-Maintenance-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api(projects.maintenanceApi)
    api(projects.adventure) {
        targetConfiguration = "shadow"
    }
    compileOnly("net.luckperms:api:5.3")
    compileOnly("net.minecrell:ServerListPlus:3.4.9-SNAPSHOT")
    compileOnly("com.google.guava:guava:29.0-jre")
    compileOnly("com.google.code.gson:gson:2.8.5")
    compileOnly("org.yaml:snakeyaml:1.23")
}

java {
    withJavadocJar()
}
