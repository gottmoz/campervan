plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val npmCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"

tasks.register<Exec>("buildHmi") {
    workingDir = file("../hmi")
    commandLine(npmCommand, "run", "build")
}

tasks.register<Copy>("copyHmiToAssets") {
    dependsOn("buildHmi")
    doFirst {
        delete(layout.projectDirectory.dir("src/main/assets/hmi"))
    }
    from(file("../hmi/dist"))
    into(layout.projectDirectory.dir("src/main/assets/hmi"))
}

android {
    namespace = "se.gottmoz.camperagent"
    compileSdk = 35

    defaultConfig {
        applicationId = "se.gottmoz.camperagent"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("com.github.mik3y:usb-serial-for-android:3.10.0")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

tasks.named("preBuild") {
    dependsOn("copyHmiToAssets")
}
