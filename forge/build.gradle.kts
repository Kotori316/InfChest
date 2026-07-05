import com.kotori316.plugin.cf.CallVersionCheckFunctionTask
import com.kotori316.plugin.cf.CallVersionFunctionTask

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.kotori316.common")
    id("com.kotori316.dg")
    id("signing")
    alias(libs.plugins.forge)
    alias(libs.plugins.publish.all)
    alias(libs.plugins.cf)
}

val modId = project.property("mod_id") as String
val minecraftVersion = project.property("minecraftVersion") as String
val releaseMode = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean().not()

project.evaluationDependsOn(project.project(":genData:commonData").path)

base {
    version = project.property("modVersion") as String
    group = "com.kotori316"
    archivesName = "${project.property("baseName")}-Forge-$minecraftVersion"
}

sourceSets {
    val mainSourceSet = main.get()

    val dataGenSourceSet = genData.get()
    create("runDataGen") {
        val sourceSet = this
        project.configurations {
            named(sourceSet.compileClasspathConfigurationName) {
                extendsFrom(
                    project.configurations.named(mainSourceSet.compileClasspathConfigurationName).get(),
                    project.configurations.named(dataGenSourceSet.compileClasspathConfigurationName).get(),
                )
            }
            named(sourceSet.runtimeClasspathConfigurationName) {
                extendsFrom(
                    project.configurations.named(mainSourceSet.runtimeClasspathConfigurationName).get(),
                )
            }
        }
    }
}

minecraft {
    runs {
        configureEach {
            workingDir.convention(layout.projectDirectory.dir("run"))
            val mixinRefMap =
                layout.buildDirectory.map { it.file("createSrgToMcp/output.srg").asFile.absolutePath }.get()
            systemProperty("mixin.env.remapRefMap", "true")
            systemProperty("mixin.env.refMapRemappingFile", mixinRefMap)
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
            systemProperty("eventbus.api.strictRuntimeChecks", "true")
            systemProperty("mixin.debug.export", "true")
            systemProperty("terminal.ansi", "true")
            if (System.getProperty("os.name").startsWith("Mac")) {
                jvmArgs("-XstartOnFirstThread")
            }
        }
        register("client") {
        }

        register("server") {
        }

        register("data") {
            // Run with `./gradlew :forge:runRunDataGenData`
            workingDir.convention(project.layout.buildDirectory.dir("dataGen"))
            args("--mod", modId, "--all", "--output", file("src/generated/resources/"))
        }
    }
}

tasks.processResources {
    from(project(":common").sourceSets.getAt("main").resources)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    exclusiveContent {
        forRepository {
            maven {
                name = "Sponge"
                url = uri("https://repo.spongepowered.org/repository/maven-public")
            }
        }
        filter {
            includeGroupAndSubgroups("org.spongepowered")
        }
    }
    mavenCentral()
}

dependencies {
    // See com.kotori316.common.gradle.kts for repositories
    implementation(minecraft.dependency("net.minecraftforge:forge:${libs.versions.forge.version.get()}"))
    compileOnly(project(":common"))
    testCompileOnly(project(":common"))
    // Mixin
    annotationProcessor("net.minecraftforge:eventbus-validator:7.0.5")

    compileOnly(libs.ae2.forge)
    compileOnly(libs.jade.forge)
    compileOnly(libs.top.forge)
    // compileOnly(fg.deobf("mcp.mobius.waila:wthit-api:forge-${libs.versions.wthit.get()}"))
    // runtimeOnly(fg.deobf("mcp.mobius.waila:wthit:forge-${libs.versions.wthit.get()}"))
    // runtimeOnly(fg.deobf("lol.bai:badpackets:forge-${libs.versions.badpackets.get()}"))
    // implementation fg.deobf("curse.maven:StorageBox-mod-419839:3430254".toLowerCase())
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4") {
        version {
            strictly("5.0.4")
        }
    }
    implementation(libs.debug.util.forge) {
        isTransitive = false
    }
}

tasks.withType(JavaCompile::class) {
    source(project(":common").sourceSets.getAt("main").allSource)
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
                "MixinConfigs" to "${modId}.mixins.json",
                "Automatic-Module-Name" to modId,
            )
        )
    }
}

val jksSignJar = tasks.register("jksSignJar") {
    dependsOn("jar")
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
    modLoaders.add("forge")
    dryRun = !releaseMode
    displayName = "${project.version}-forge"

    curseforge {
        accessToken = project.findProperty("curseforge_additional-enchanted-miner_key")?.toString()
            ?: System.getenv("CURSE_TOKEN") ?: ""
        projectId = "312222"
        minecraftVersions = listOf(minecraftVersion)
        client = true
        server = true
    }

    modrinth {
        accessToken = project.findProperty("modrinthToken")?.toString() ?: System.getenv("MODRINTH_TOKEN") ?: ""
        projectId = "lmosnPHi"
        minecraftVersions = listOf(minecraftVersion)
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
    gameVersion = minecraftVersion
    platform = "forge"
    platformVersion = libs.versions.forge.version.get()
    modName = modId
    changelog = "For $minecraftVersion"
    isDryRun = !releaseMode
    homepage = "https://www.curseforge.com/minecraft/mc-mods/infchest"
}

tasks.register("checkReleaseVersion", CallVersionCheckFunctionTask::class) {
    gameVersion = minecraftVersion
    platform = "forge"
    modName = modId
    version = project.version as String
    failIfExists = releaseMode
}

sourceSets.forEach {
    val dir = layout.buildDirectory.dir("sourcesSets/${it.name}")
    it.output.setResourcesDir(dir)
    it.java.destinationDirectory = dir
}

tasks.named("compileRunDataGenJava", JavaCompile::class) {
    dependsOn("processGenDataResources")
    project.findProject(":common")?.let {
        source(it.sourceSets.main.get().java)
    }
    project.findProject(":genData:commonData")?.let {
        source(it.sourceSets.main.get().java)
    }
    source(project.sourceSets.main.get().java)
    source(project.sourceSets.genData.get().java)
}

tasks.named("processRunDataGenResources", ProcessResources::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    project.findProject(":common")?.let {
        from(it.sourceSets.main.get().resources)
    }
    project.findProject(":genData:commonData")?.let {
        from(it.sourceSets.main.get().resources)
    }
    from(project.sourceSets.main.get().resources)
    from(project.sourceSets.genData.get().resources)
}
