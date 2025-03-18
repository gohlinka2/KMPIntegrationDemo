import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id(kmpLibs.plugins.kotlinMultiplatform.get().pluginId)
    id(kmpLibs.plugins.androidLibrary.get().pluginId)
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            //put your multiplatform dependencies here
        }
        commonTest.dependencies {
            implementation(kmpLibs.kotlin.test)
        }
    }
}

android {
    namespace = "cz.hlinka.kmpintegrationdemo"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
