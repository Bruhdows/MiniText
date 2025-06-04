plugins {
    id("java")
    id("io.freefair.lombok") version "8.13.1"
    id("com.gradleup.shadow") version "9.0.0-beta15"
}

group = "com.bruhdows"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.kyori:adventure-text-serializer-legacy:4.21.0")
}