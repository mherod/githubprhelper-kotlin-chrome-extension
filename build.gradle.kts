import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.js") version "1.3.72"
}

group = "dev.herod.browser.githubprhelper"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.5")
}

kotlin.target.browser { }

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
    register("copyCompiledJs", Copy::class) {
        dependsOn("assemble")
        from("build/distributions") {
            include("**/*.js")
            rename("(.*)", "build/inject.js")
        }
        into("extension")
        includeEmptyDirs = false
    }
    findByName("build")?.dependsOn("copyCompiledJs")
}
