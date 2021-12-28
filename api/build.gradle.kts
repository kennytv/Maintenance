plugins {
    id("maintenance.shadow-conventions")
}

dependencies {
    compileOnlyApi("org.jetbrains:annotations:23.0.0")
}

java {
    withJavadocJar()
}
