import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseProject

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("maven-publish")
    id("signing")
    id("java")
    id("net.minecraftforge.gradle") version ("[6.0,6.2)")
    id("org.spongepowered.mixin") version ("0.7.+")
    id("org.parchmentmc.librarian.forgegradle") version ("1.+")
    id("com.matthewprenger.cursegradle") version ("1.4.0")
    id("com.modrinth.minotaur") version ("2.+")
    id("com.kotori316.plugin.cf") version ("3.+")
}

val modId = project.property("mod_id") as String
val minecraft = project.property("minecraftVersion") as String
val releaseMode = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean().not()

base {
    version = project.property("modVersion") as String
    group = "com.kotori316"
    archivesName = "${project.property("baseName")}-Forge-$minecraft"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

println(
    "Java: " + System.getProperty("java.version") +
            " JVM: " + System.getProperty("java.vm.version") +
            "(" + System.getProperty("java.vendor") + ")" +
            " Arch: " + System.getProperty("os.arch")
)

minecraft {
    // The mappings can be changed at any time, and must be in the following format.
    // snapshot_YYYYMMDD   Snapshot are built nightly.
    // stable_#            Stables are built at the discretion of the MCP team.
    // Use non-default mappings at your own risk. they may not always work.
    // Simply re-run your setup task after changing the mappings to update your workspace.
    mappings(
        mapOf(
            "channel" to "parchment", "version" to ("${project.property("parchmentMapping")}-${minecraft}")
        )
    )
    // mappings channel: "official", version: "1.18.2"
    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.

    // accessTransformer = file("src/main/resources/META-INF/accesstransformer.cfg")

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        configureEach{
            val mixinRefMap = layout.buildDirectory.map { it.file("createSrgToMcp/output.srg").asFile.absolutePath }.get()
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", mixinRefMap)
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")
            property("mixin.debug.export", "true")
            property("terminal.ansi", "true")

            mods {
                create(modId) {
                    source(sourceSets.getAt("main"))
                    source(project(":common").sourceSets.getAt("main"))
                }
            }
        }
        create("client") {
            workingDirectory = "run"
        }

        create("server") {
            workingDirectory = "run"
        }

        create("data") {
            workingDirectory = "run"
            args("--mod", modId, "--all", "--output", file("src/generated/resources/"))
        }
    }
}

tasks.processResources {
    from(project(":common").sourceSets.getAt("main").resources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    maven {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
        url = uri("https://modmaven.dev/")
        content {
            includeModule("appeng", "appliedenergistics2-forge")
        }
    }
    maven {
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

dependencies {
    minecraft("net.minecraftforge:forge:${project.property("forgeVersion")}")
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))
    // Mixin
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    compileOnly(fg.deobf("appeng:appliedenergistics2-forge:${project.property("ae2Version")}"))
    implementation(fg.deobf("curse.maven:jade-324717:${project.property("jade_forge_id")}"))
    compileOnly(fg.deobf("curse.maven:the-one-probe-245211:${project.property("top_id")}"))
    compileOnly(fg.deobf("mcp.mobius.waila:wthit-api:forge-${project.property("wthit_forge_version")}"))
    implementation(
        fg.deobf(
            "com.refinedmods:refinedstorage:${project.property("rsVersion")}",
            closureOf<ExternalModuleDependency> {
                isTransitive = false
            })
    )
    // runtimeOnly(fg.deobf("mcp.mobius.waila:wthit:forge-${project.wthit_version}"))
    // runtimeOnly(fg.deobf("lol.bai:badpackets:forge-${project.badpackets_forge_version}"))
    // implementation fg.deobf("curse.maven:StorageBox-mod-419839:3430254".toLowerCase())
}

tasks.withType(JavaCompile::class) {
    source(project(":common").sourceSets.getAt("main").allSource)
}

mixin {
    add(sourceSets.getAt("main"), "mixins.${modId}.refmap.json")
}

// Example for how to get properties into the manifest for reading by the runtime..
tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to project.name,
                "Specification-Vendor" to "Kotori316",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version.toString(),
                "Implementation-Vendor" to "Kotori316",
                "Implementation-Timestamp" to ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                // "MixinConfigs"            : "${modId}.mixins.json",
                "Automatic-Module-Name" to modId,
            )
        )
    }
}

// Example configuration to allow publishing using the maven-publish task
// we define a custom artifact that is sourced from the reobfJar output task
// and then declare that to be published
// Note you'll need to add a repository here
val reobfFile = layout.buildDirectory.file("reobfJar/output.jar")
val reobfArtifact = artifacts.add("default", reobfFile) {
    type = "jar"
    builtBy("reobfJar")
}

val jksSignJar by tasks.register("jksSignJar") {
    dependsOn("reobfJar")
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        ant.withGroovyBuilder {
            "signjar"(
                "jar" to tasks.jar.get().archiveFile.get(),
                "alias" to project.findProperty("jarSign.keyAlias"),
                "keystore" to project.findProperty("jarSign.keyLocation"),
                "storepass" to project.findProperty("jarSign.storePass"),
                "sigalg" to "Ed25519",
                "digestalg" to "SHA-256",
                "tsaurl" to "http://timestamp.digicert.com",
            )
        }
    }
}

val jar by tasks.jar

jar.finalizedBy(jksSignJar)

val srcJar by tasks.register("srcJar", Jar::class) {
    from(sourceSets.getAt("main").allSource)
    archiveClassifier.set("sources")
}

val deobfJar by tasks.register("deobfJar", Jar::class) {
    from(sourceSets.getAt("main").output)
    archiveClassifier.set("deobf")
}

// Tell the artifact system about our extra jars
artifacts {
    archives(srcJar.archiveFile)
    archives(deobfJar.archiveFile)
}

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = "312222"
        changelogType = "markdown"
        changelog = file("../temp_changelog.md")
        addGameVersion(minecraft)
        addGameVersion("Forge")
        releaseType = "release"
        mainArtifact(jar.archiveFile.get())
        addArtifact(srcJar.archiveFile.get())
        addArtifact(deobfJar.archiveFile.get())
    })
    options(closureOf<com.matthewprenger.cursegradle.Options> {
        curseGradleOptions.debug = !releaseMode
        curseGradleOptions.javaVersionAutoDetect = false
        curseGradleOptions.forgeGradleIntegration = false
    })
}

modrinth {
    token.set((project.findProperty("modrinthToken") ?: System.getenv("MODRINTH_TOKEN") ?: "") as String)
    projectId = "infchest"
    versionType = "release"
    versionName = "${project.version}-forge"
    versionNumber.set(project.version.toString())
    uploadFile = jar
    additionalFiles = listOf(
        deobfJar,
        srcJar,
    )
    gameVersions = listOf(minecraft)
    loaders = listOf("forge")
    changelog = file("../temp_changelog.md").useLines { it.joinToString(System.lineSeparator()).split("# ")[1] }
    debugMode = !releaseMode
    dependencies {
    }
}

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
        }
    }
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get().lowercase()
            artifact(srcJar)
            artifact(deobfJar)
            artifact(reobfArtifact)
        }
    }
}

tasks.register("copyToDrive", Copy::class) {
    dependsOn("build")
    from(jar.archiveFile, deobfJar.archiveFile, srcJar.archiveFile)
    into(file(System.getenv("drive_path") ?: "."))
    onlyIf {
        System.getenv("drive_path") != null &&
                file(System.getenv("drive_path")).exists()
    }
}

signing {
    sign(publishing.publications)
    sign(jar, deobfJar, srcJar)
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class).configureEach {
    onlyIf {
        hasGpgSignature
    }
}

tasks.withType(AbstractPublishToMaven::class).configureEach {
    if (hasGpgSignature) {
        dependsOn(":forge:signJar")
        dependsOn(":forge:signSrcJar")
        dependsOn(":forge:signDeobfJar")
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = "forge"
    platformVersion = project.property("forgeVersion").toString()
    modName = modId
    changelog = "For $minecraft"
    isDryRun = !releaseMode
    homepage = "https://www.curseforge.com/minecraft/mc-mods/infchest"
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = minecraft
    platform = "forge"
    modName = modId
    version = project.version as String
    failIfExists = releaseMode
}
