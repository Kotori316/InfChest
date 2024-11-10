plugins {
    id("java")
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
            srcDir("src/generated/resources")
        }
    }

    create("genData") {
        val sourceSet = this
        project.configurations {
            named(sourceSet.compileClasspathConfigurationName) {
                extendsFrom(project.configurations.compileClasspath.get())
            }
            named(sourceSet.runtimeClasspathConfigurationName) {
                extendsFrom(project.configurations.runtimeClasspath.get())
            }
        }
    }
}

configurations {
    create("dataGenRuntime")
}

dependencies {
    "genDataImplementation"(project.sourceSets.main.get().output)
    "genDataImplementation"(project.project(":genData:commonData"))
}

if (project.name == "neoforge") {
    tasks.named("compileGenDataJava", JavaCompile::class) {
        source(project.project(":genData:commonData").sourceSets.main.get().allSource)
    }
}
