plugins {
    id("com.kotori316.common")
    alias(libs.plugins.vanilla.gradle)
}

base {
    archivesName = "${project.property("baseName")}-Common-${project.property("minecraftVersion")}"
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }
}

minecraft {
    version(project.property("minecraftVersion") as String)
}

dependencies {
    compileOnly(libs.mixin)
    implementation(libs.jsr305)
}
