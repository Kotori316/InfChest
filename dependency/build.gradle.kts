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
    implementation(
        group = "net.neoforged",
        name = "neoforge",
        version = project.property("neo_version").toString()
    )
}
