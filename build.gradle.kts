plugins {
    id(kmpLibs.plugins.androidLibrary.get().pluginId) version kmpLibs.versions.agp.get() apply false
    id(kmpLibs.plugins.kotlinMultiplatform.get().pluginId) version kmpLibs.versions.kotlin.get() apply false
}
