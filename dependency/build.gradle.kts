plugins {
    java
}

repositories {
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

dependencies {
    // NeoForge
    implementation(libs.neoforge)
}
