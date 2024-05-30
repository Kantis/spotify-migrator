plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs()
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.ktorClient.core)
            implementation(libs.ktorClient.contentNegotiation)
            implementation(libs.ktorSerialization.kotlinxJson)
        }

        desktopMain.dependencies {
            implementation(libs.ktorClient.cio)
        }
    }
}
