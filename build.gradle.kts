plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
}

group = "com.bruhdows"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.adventure.text.serializer.legacy)
    implementation(libs.adventure.text.serializer.plain)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}
