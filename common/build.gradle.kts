plugins {
    id("java")
    id("org.spongepowered.gradle.vanilla") version ("0.2.1-SNAPSHOT")
}

base {
    archivesName = "${project.property("baseName")}-Common-${project.property("minecraftVersion")}"
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8.5")
    implementation(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.1")
}
