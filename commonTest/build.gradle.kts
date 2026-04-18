plugins {
    id("com.kotori316.common")
    id("org.spongepowered.gradle.vanilla") version ("0.3.2")
}

base {
    archivesName = "${project.property("baseName")}-CommonTest-${project.property("minecraftVersion")}"
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    compileOnly("org.spongepowered:mixin:0.8.7")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation(project(":common"))
}
