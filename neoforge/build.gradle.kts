import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common")
    id("com.kotori316.dg")
    signing
    alias(libs.plugins.neoforge)
    alias(libs.plugins.publish.all)
    alias(libs.plugins.cf)
}

val modId = project.property("mod_id") as String
val minecraft = project.property("minecraftVersion") as String
val releaseMode = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean().not()

project.evaluationDependsOn(project.project(":genData:commonData").path)

base {
    version = project.property("modVersion") as String
    group = "com.kotori316"
    archivesName = "${project.property("baseName")}-NeoForge-$minecraft"
}

sourceSets {
    create("gameTest") {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
    }
}

neoForge {
    version = libs.versions.neo.version.get()

    mods {
        create(modId) {
            sourceSet(sourceSets.getByName("main"))
        }
        create("gameTest") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("gameTest"))
        }
        create("data") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("genData"))
        }
    }

    runs {
        create("client") {
            client()
            gameDirectory = file("run")
            systemProperties.put("mixin.debug.export", "true")
            if (!System.getProperty("os.name").contains("windows", ignoreCase = true)) {
                jvmArguments.add("-XstartOnFirstThread")
            }
            loadedMods = listOf(
                mods[modId]
            )
        }
        create("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("runs/gameTestServer")
            loadedMods = listOf(
                mods["gameTest"]
            )
        }
        create("serverData") {
            serverData()
            gameDirectory = project.file("runs/serverData")
            programArguments = listOf(
                "--mod",
                modId,
                "--output",
                file("src/generated/resources/").toString(),
                "--existing",
                file("src/main/resources/").toString()
            )
            sourceSet = project.sourceSets.getByName("genData")
            loadedMods = listOf(
                mods["data"]
            )
        }
        create("commonData") {
            clientData()
            gameDirectory = project.file("runs/clientData")
            programArguments = listOf(
                "--mod",
                modId,
                "--output",
                project.project(":common").file("src/generated/resources/").toString(),
                "--existing",
                project.project(":common").file("src/main/resources/").toString()
            )
            sourceSet = project.sourceSets.getByName("genData")
            loadedMods = listOf(
                mods["data"]
            )
        }
    }

    unitTest {
        enable()
        testedMod = mods[modId]
        loadedMods = listOf(
            mods[modId]
        )
    }
}

dependencies {
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))
    compileOnly(libs.jade.neoforge)
    compileOnly("mcp.mobius.waila:wthit-api:neo-${libs.versions.wthit.get()}")
    /*runtimeOnly(
        group = "mcp.mobius.waila",
        name = "wthit",
        version = "neo-${libs.versions.wthit.get()}"
    )*/
    compileOnly(libs.top.neoforge)
    compileOnly(libs.ae2.neoforge) {
        isTransitive = false
    }
    implementation(libs.debug.util.neoforge)

    "gameTestImplementation"(project.project(":neoforge"))
    "gameTestImplementation"(project(":commonTest"))

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("net.neoforged:testframework:${libs.versions.neo.version.get()}")
}

tasks {
    processResources {
        from(project(":common").sourceSets.main.get().resources)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named("compileJava", JavaCompile::class) {
        source(project(":common").sourceSets.main.get().allSource)
    }

    named("compileGameTestJava", JavaCompile::class) {
        source(project(":commonTest").sourceSets.main.get().allSource)
    }

    jar {
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

    test {
        useJUnitPlatform()
    }
}

val jksSignJar by tasks.register("jksSignJar") {
    dependsOn(tasks.jar)
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

tasks.jar {
    finalizedBy(jksSignJar)
}

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    additionalFiles = files(tasks.named("sourcesJar"))
    changelog = provider { file("../temp_changelog.md").readText() }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("neoforge")
    displayName = "${project.version}-neoforge"
    dryRun = !releaseMode

    curseforge {
        accessToken = project.findProperty("curseforge_additional-enchanted-miner_key")?.toString()
            ?: System.getenv("CURSE_TOKEN") ?: ""
        projectId = "312222"
        minecraftVersions = listOf(minecraft)
    }

    modrinth {
        accessToken = project.findProperty("modrinthToken")?.toString() ?: System.getenv("MODRINTH_TOKEN") ?: ""
        projectId = "lmosnPHi"
        minecraftVersions = listOf(minecraft)
        changelog = provider { file("../temp_changelog.md").readText().split("# ").getOrNull(1) }
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class) {
            artifactId = base.archivesName.get().lowercase()
            from(components.getAt("java"))
        }
    }
}

signing {
    sign(publishing.publications)
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class).configureEach {
    onlyIf {
        hasGpgSignature
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = "neoforge"
    platformVersion = libs.versions.neo.version.get()
    modName = modId
    changelog = "For $minecraft"
    isDryRun = !releaseMode
    homepage = "https://modrinth.com/mod/infchest"
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = minecraft
    platform = "neoforge"
    modName = modId
    version = project.version as String
    failIfExists = releaseMode
}
