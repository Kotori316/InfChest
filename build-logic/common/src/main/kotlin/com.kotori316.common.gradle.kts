import java.time.ZoneOffset
import java.time.ZonedDateTime

plugins {
    id("java")
    id("maven-publish")
}

println(
    "${project.name} Java: ${
        System.getProperty("java.version")
    } JVM: ${
        System.getProperty("java.vm.version")
    } (${
        System.getProperty("java.vendor")
    }) Arch: ${
        System.getProperty("os.arch")
    }"
)

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

val minecraftVersion = project.property("minecraftVersion") as String
val currentDate: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", project.version)
    inputs.property("minecraftVersion", minecraftVersion)
    inputs.property("currentYear", currentDate.year)

    listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json").forEach {
        filesMatching(it) {
            expand(
                mapOf(
                    "version" to project.version,
                    "minecraftVersion" to minecraftVersion,
                    "currentYear" to currentDate.year.toString(),
                )
            )
        }
    }
}

repositories {
    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")
    }
    maven {
        name = "Curse"
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        name = "What The Hell Is That"
        url = uri("https://maven2.bai.lol")
        content {
            includeModule("mcp.mobius.waila", "wthit-api")
            includeModule("mcp.mobius.waila", "wthit")
            includeModule("lol.bai", "badpackets")
        }
    }
    maven {
        name = "ModMaven"
        url = uri("https://modmaven.dev/")
        content {
            includeModule("appeng", "appliedenergistics2-neoforge")
            includeModule("appeng", "appliedenergistics2-fabric")
            includeModule("appeng", "appliedenergistics2-forge")
        }
    }
    maven {
        name = "Kotori316 Plugin"
        url = uri("https://maven.kotori316.com/")
        content {
            includeGroup("com.kotori316")
        }
    }
    maven {
        name = "Mixin"
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        url = uri("https://maven.pkg.github.com/refinedmods/refinedstorage")
        credentials {
            username = "anything"
            password = "\u0067hp_oGjcDFCn8jeTzIj4Ke9pLoEVtpnZMP4VQgaX"
        }
        content {
            includeGroup("com.refinedmods")
        }
    }
}

if (project.name != "common") {
    val releaseMode = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean().not()
    publishing {
        if (releaseMode) {
            repositories {
                maven {
                    name = "AzureRepository"
                    url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
                    val user = project.findProperty("azureUserName") ?: System.getenv("AZURE_USER_NAME") ?: ""
                    val pass = project.findProperty("azureToken") ?: System.getenv("AZURE_TOKEN") ?: "TOKEN"
                    credentials {
                        username = user.toString()
                        password = pass.toString()
                    }
                }
                if (System.getenv("CLOUDFLARE_S3_ENDPOINT") != null) {
                    val r2AccessKey =
                        (project.findProperty("r2_access_key") ?: System.getenv("R2_ACCESS_KEY") ?: "") as String
                    val r2SecretKey =
                        (project.findProperty("r2_secret_key") ?: System.getenv("R2_SECRET_KEY") ?: "") as String
                    maven {
                        name = "kotori316-maven"
                        url = uri("s3://kotori316-maven")
                        credentials(AwsCredentials::class) {
                            accessKey = r2AccessKey
                            secretKey = r2SecretKey
                        }
                    }
                }
            }
        }
    }
}
