plugins {
    id("com.kotori316.common")
    id("org.spongepowered.gradle.vanilla") version ("0.2.2")
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    implementation(project(":common"))
}
