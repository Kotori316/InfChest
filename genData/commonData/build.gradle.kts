plugins {
    id("com.kotori316.common")
    alias(libs.plugins.vanilla.gradle)
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    implementation(project(":common"))
}
