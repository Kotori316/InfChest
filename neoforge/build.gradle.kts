import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common")
    id("com.kotori316.dg")
    signing
    id("net.neoforged.moddev") version ("2.0.140")
    id("me.modmuss50.mod-publish-plugin") version ("1.1.0")
    id("com.kotori316.plugin.cf") version ("3.+")
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
    version = project.property("neo_version").toString()

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

    parchment {
        minecraftVersion = project.property("parchment_mapping_mc").toString()
        mappingsVersion = project.property("parchment_mapping_version").toString()
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

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.ow2.asm" && requested.name.startsWith("asm")) {
            useVersion("9.7")
        }
    }
}

dependencies {
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))
    compileOnly(
        group = "curse.maven",
        name = "jade-324717",
        version = project.property("jade_neoforge_id") as String
    )
    compileOnly(
        group = "mcp.mobius.waila",
        name = "wthit-api",
        version = "neo-${project.property("wthit_neoforge_version")}"
    )
    /*runtimeOnly(
        group = "mcp.mobius.waila",
        name = "wthit",
        version = "neo-${project.property("wthit_neoforge_version")}"
    )*/
    compileOnly(
        group = "curse.maven",
        name = "the-one-probe-245211",
        version = project.property("top_neoforge_id") as String
    )
    compileOnly(
        group = "appeng",
        name = "appliedenergistics2",
        version = project.property("ae2_neoforge_version") as String,
    ) {
        isTransitive = false
    }
    implementation("com.kotori316:debug-utility-neoforge:${project.property("debug_util_version")}")

    "gameTestImplementation"(project.project(":neoforge"))
    "gameTestImplementation"(project(":commonTest"))

    testImplementation(platform("org.junit:junit-bom:6.0.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(
        group = "net.neoforged",
        name = "testframework",
        version = project.property("neo_version").toString()
    )
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

val jar by tasks.jar

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

publishMods {
    file = jar.archiveFile
    additionalFiles.from(srcJar.archiveFile, deobfJar.archiveFile)
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
        dependsOn(tasks.named("signJar"))
        dependsOn(tasks.named("signSrcJar"))
        dependsOn(tasks.named("signDeobfJar"))
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = "neoforge"
    platformVersion = project.property("neo_version").toString()
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
