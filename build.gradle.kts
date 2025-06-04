plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
}

group = "com.bruhdows"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.adventure.text.serializer.legacy)
}
