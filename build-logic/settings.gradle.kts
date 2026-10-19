dependencyResolutionManagement {
    repositories {
        google()
        // Order: docs/architecture.md#repositories
        maven("https://maven-central.storage-download.googleapis.com/maven2") { name = "GoogleMavenCentralMirror" }
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
