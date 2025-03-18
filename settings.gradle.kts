enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    defaultLibrariesExtensionName = "kmpLibs"
}

rootProject.name = "KMPIntegrationDemo"
include(":shared")
