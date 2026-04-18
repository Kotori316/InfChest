plugins {
    id("com.kotori316.common")
    alias(libs.plugins.vanilla.gradle)
}

base {
    archivesName = "${project.property("baseName")}-CommonTest-${project.property("minecraftVersion")}"
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    compileOnly(libs.mixin)
    implementation(libs.jsr305)
    implementation(project(":common"))
}
