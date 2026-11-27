plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SumaStudioUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core-math"))
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            implementation(compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = false
    packageOfResClass = "com.e25solutions.sumastudio.ui.resources"
    generateResClass = always
}

android {
    namespace = "com.e25solutions.sumastudio.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
