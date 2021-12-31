plugins {
    id("maintenance.shadow-conventions")
}

dependencies {
    compileOnlyApi(libs.jetbrainsAnnotations)
}

java {
    withJavadocJar()
}
