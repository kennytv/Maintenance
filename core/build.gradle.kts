plugins {
    id("net.kyori.blossom")
}

blossom {
    replaceToken("\$VERSION", project.version)
    replaceToken("\$IMPL_VERSION", "git-Maintenance-${project.version}:${rootProject.latestCommitHash()}")
}

dependencies {
    api(projects.maintenanceApi)
    api(projects.adventure.adventureApi) {
        targetConfiguration = "shadow"
    }
    compileOnly(libs.luckperms)
    compileOnly(libs.serverlistplus)
    compileOnly(libs.guava)
    compileOnly(libs.gson)
    compileOnly(libs.snakeyaml)
}

java {
    withJavadocJar()
}
