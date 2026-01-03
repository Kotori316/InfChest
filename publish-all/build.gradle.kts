import okhttp3.internal.toImmutableList

plugins {
    id("me.modmuss50.mod-publish-plugin") version ("1.1.0")
}

val releaseDebug = (System.getenv("RELEASE_DEBUG") ?: "true").toBoolean()
val modVersion: String by project

publishMods {
    dryRun = releaseDebug
    version = modVersion
    displayName = "v${modVersion} for ${project.findProperty("minecraftVersion")}"
    changelog = provider { file("../temp_changelog.md").readText().split("# ").getOrNull(1) }
    type = if (modVersion.contains("SNAPSHOT")) BETA else STABLE

    val releaseJarFiles = getReleaseJarFiles()
    file = releaseJarFiles.first()
    additionalFiles.from(
        *releaseJarFiles.drop(1).toTypedArray()
    )

    github {
        repository = "Kotori316/InfChest"
        accessToken = project.findProperty("githubToken") as? String ?: System.getenv("REPO_TOKEN") ?: ""
        commitish = project.property("branch") as? String
        tagName = "v${modVersion}"
    }
}

fun getReleaseJarFiles(): List<Provider<RegularFile>> {
    val list = mutableListOf<Provider<RegularFile>>()
    if (!(System.getenv("DISABLE_FORGE") ?: "false").toBoolean()) {
        list.add(project(":forge").tasks.named("jar", AbstractArchiveTask::class).flatMap { it.archiveFile })
        list.add(project(":forge").tasks.named("srcJar", AbstractArchiveTask::class).flatMap { it.archiveFile })
    }
    if (!(System.getenv("DISABLE_FABRIC") ?: "false").toBoolean()) {
        list.add(project(":fabric").tasks.named("remapJar", AbstractArchiveTask::class).flatMap({ it.archiveFile }))
        list.add(
            project(":fabric").tasks.named("remapSourcesJar", AbstractArchiveTask::class).flatMap({ it.archiveFile })
        )
    }
    if (!(System.getenv("DISABLE_NEOFORGE") ?: "false").toBoolean()) {
        list.add(project(":neoforge").tasks.named("jar", AbstractArchiveTask::class).flatMap { it.archiveFile })
        list.add(project(":neoforge").tasks.named("srcJar", AbstractArchiveTask::class).flatMap { it.archiveFile })
    }
    return list.toImmutableList()
}
