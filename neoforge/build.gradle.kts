import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseProject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common")
    signing
    id("net.neoforged.gradle.userdev") version ("[7.0.57, 8)")
    id("net.neoforged.gradle.mixin") version ("[7.0.57, 8)")
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
    archivesName = "${project.property("baseName")}-NeoForge-$minecraft"
}

sourceSets {
    create("gameTest") {
        compileClasspath += main.get().compileClasspath
        runtimeClasspath += main.get().runtimeClasspath
    }
}

minecraft {
    mappings {
        version("minecraft", project.property("minecraftVersion") as String)
    }
}

runs {
    create("client") {
        workingDirectory = file("run")
        systemProperties.put("neoforge.enabledGameTestNamespaces", modId)
        systemProperties.put("mixin.debug.export", "true")
        jvmArguments.add("-XstartOnFirstThread")
        // modSources.add(project.sourceSets.main)
    }
    create("gameTestServer") {
        workingDirectory = file("runs/gameTestServer")
        systemProperties.put("neoforge.enabledGameTestNamespaces", "$modId,minecraft")
        modSources.add(project.sourceSets.getByName("gameTest"))
    }
}

dependencies {
    // See com.kotori316.common.gradle.kts for repositories
    implementation("net.neoforged:neoforge:${project.property("neo_version")}")
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
        version = "neo-${project.property("wthit_forge_version")}"
    )
    compileOnly(
        group = "curse.maven",
        name = "the-one-probe-245211",
        version = project.property("top_neoforge_id") as String
    )
    implementation(
        group = "appeng",
        name = "appliedenergistics2-neoforge",
        version = project.property("ae2_neoforge_version") as String,
    ) {
        isTransitive = false
    }

    "gameTestImplementation"(project(":neoforge"))
}

tasks {
    processResources {
        from(project(":common").sourceSets.main.get().resources)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    named("compileJava", JavaCompile::class) {
        source(project(":common").sourceSets.main.get().allSource)
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

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = "312222"
        changelogType = "markdown"
        changelog = file("../temp_changelog.md")
        addGameVersion(minecraft)
        addGameVersion("NeoForge")
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
    loaders = listOf("neoforge")
    changelog = file("../temp_changelog.md").useLines { it.joinToString(System.lineSeparator()).split("# ")[1] }
    debugMode = !releaseMode
    dependencies {
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
