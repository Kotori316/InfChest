import java.time.ZoneOffset
import java.time.ZonedDateTime

plugins {
    id("java")
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
val currentDate = ZonedDateTime.now(ZoneOffset.UTC)

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
