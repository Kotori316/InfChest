pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Forge"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "Parchment"
            url = uri("https://maven.parchmentmc.org")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases")
        }
        maven {
            name = "Sponge Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        maven {
            name = "Kotori316 Plugin"
            url = uri("https://storage.googleapis.com/kotori316-maven-storage/maven/")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

includeBuild("build-logic")
include("common")
include("commonTest")
include("genData:commonData")
include("dependency")
include("publish-all")
if (!(System.getenv("DISABLE_FORGE") ?: "false").toBoolean()) {
    include("forge")
}
if (!(System.getenv("DISABLE_FABRIC") ?: "false").toBoolean()) {
    include("fabric")
}
if (!(System.getenv("DISABLE_NEOFORGE") ?: "false").toBoolean()) {
    include("neoforge")
}
