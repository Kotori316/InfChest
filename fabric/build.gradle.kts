import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import org.gradle.jvm.tasks.Jar

plugins {
    id("com.kotori316.common")
    id("signing")
    // https://maven.fabricmc.net/net/fabricmc/fabric-loom/
    id("fabric-loom") version ("1.8.4")
    id("com.matthewprenger.cursegradle") version ("1.4.0")
    id("com.modrinth.minotaur") version ("2.+")
    id("com.kotori316.plugin.cf") version ("3.+")
}

val baseName: String by project
val minecraft: String by extra { project.property("minecraftVersion") as String }
val modId: String by extra { project.property("mod_id") as String }
val modVersion: String by project
val releaseMode: Boolean = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean().not()

base {
    archivesName = "${baseName}-Fabric-${minecraft}"
    version = modVersion
    group = "com.kotori316"
}

loom {
    runs {
        named("client") {
            configName = "Client"
            runDir = "run"
        }
        create("gameTestServer") {
            configName = "GameTestServer"
            server()
            //noinspection SpellCheckingInspection
            vmArgs(
                "-ea",
                "-Dfabric-api.gametest",
                "-Dfabric-api.gametest.report-file=game_test/test-results/test/game_test.xml"
            )
            runDir = "game_test"
            source(sourceSets.getAt("test"))
        }
    }
}

dependencies {
    // See com.kotori316.common.gradle.kts for repositories
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        val parchmentMc = project.property("parchment_mapping_mc")
        val mapping = project.property("parchment_mapping_version")
        parchment("org.parchmentmc.data:parchment-$parchmentMc:$mapping@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    modCompileOnly("appeng:appliedenergistics2-fabric:${project.property("ae2_fabric_version")}") {
        isTransitive = false
    }
    //noinspection SpellCheckingInspection
    // modRuntimeOnly("teamreborn:energy:2.2.0") // For AE2
    modCompileOnly("mcp.mobius.waila:wthit-api:fabric-${project.property("wthit_fabric_version")}")
    modRuntimeOnly("mcp.mobius.waila:wthit:fabric-${project.property("wthit_fabric_version")}")
    // modRuntimeOnly("lol.bai:badpackets:fabric-${project.badpackets_fabric_version}")
    modImplementation("curse.maven:jade-324717:${project.property("jade_fabric_id")}")
    modImplementation("com.kotori316:VersionCheckerMod:${project.property("automatic_potato_version")}") {
        isTransitive = false
    }
}

tasks.processResources {
    from(project(":common").sourceSets.main.map { it.resources })
}

// ensure that the encoding is set to UTF-8, no matter what the system default is
// this fixes some edge cases with special characters not displaying correctly
// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
    source(project(":common").sourceSets.main.map { it.allSource })
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

curseforge {
    apiKey = project.findProperty("curseforge_additional-enchanted-miner_key") ?: System.getenv("CURSE_TOKEN") ?: ""
    project(closureOf<CurseProject> {
        id = "312222"
        changelogType = "markdown"
        changelog = file("../temp_changelog.md")
        addGameVersion("Fabric")
        addGameVersion(minecraft)
        releaseType = "beta"
        mainArtifact(tasks.remapJar.flatMap { it.archiveFile }.get())
        relations(closureOf<CurseRelation> {
            requiredDependency("automatic-potato")
        })
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
    versionName = "${project.version}-fabric"
    versionNumber.set(project.version.toString())
    uploadFile = tasks.remapJar.get()
    gameVersions = listOf(minecraft)
    loaders = listOf("fabric")
    changelog = file("../temp_changelog.md").useLines { it.joinToString(System.lineSeparator()).split("# ")[1] }
    debugMode = !releaseMode
    dependencies {
        required.project("automatic-potato")
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

tasks.register("checkOutput") {
    doLast {
        listOf(tasks.remapJar, tasks.jar, tasks.named("sourcesJar", Jar::class))
            .map { it.get() }
            .forEach { t ->
                println("$t -> ${t.archiveFile.get().asFile}")
            }
        println("${tasks.remapSourcesJar} -> ${tasks.remapSourcesJar.get().outputs}")
    }
}

tasks.register("copyToDrive", Copy::class) {
    dependsOn("build")
    from(tasks.remapJar.map { it.archiveFile })
    into(file(System.getenv("drive_path") ?: "."))
    onlyIf {
        System.getenv("drive_path") != null &&
                file(System.getenv("drive_path")).exists()
    }
}

val jksSignJar by tasks.register("jksSignJar") {
    dependsOn(tasks.remapJar)
    val executeCondition = project.hasProperty("jarSign.keyAlias") &&
            project.hasProperty("jarSign.keyLocation") &&
            project.hasProperty("jarSign.storePass")
    onlyIf { executeCondition }
    doLast {
        //noinspection HttpUrlsUsage
        ant.withGroovyBuilder {
            "signjar"(
                "jar" to tasks.remapJar.get().archiveFile.get(),
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

tasks.remapJar {
    finalizedBy(jksSignJar)
}

signing {
    sign(publishing.publications)
    sign(tasks.jar.get(), tasks.remapJar.get(), tasks.named("sourcesJar", Jar::class).get())
}

val hasGpgSignature = project.hasProperty("signing.keyId") &&
        project.hasProperty("signing.password") &&
        project.hasProperty("signing.secretKeyRingFile")

tasks.withType(Sign::class) {
    onlyIf {
        hasGpgSignature
    }
}

tasks.withType(AbstractPublishToMaven::class) {
    if (hasGpgSignature) {
        dependsOn(":fabric:signRemapJar")
    }
}

tasks.register("registerVersion", CallVersionFunctionTask::class) {
    functionEndpoint = CallVersionFunctionTask.readVersionFunctionEndpoint(project)
    gameVersion = minecraft
    platform = "fabric"
    platformVersion = project.property("fabric_version").toString()
    modName = modId
    changelog = "For $minecraft"
    isDryRun = !releaseMode
    homepage = "https://modrinth.com/mod/infchest"
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = minecraft
    platform = "fabric"
    modName = modId
    version = modVersion
    failIfExists = releaseMode
}
