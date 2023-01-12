import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.loloof64"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("cafe.adriel.lyricist:lyricist:1.2.2")
                implementation("io.github.wolfraam:chessgame:1.2")
                implementation("com.squareup.okio:okio:3.3.0")
                api("com.arkivanov.decompose:decompose:1.0.0-beta-01")
                api("com.arkivanov.decompose:extensions-compose-jetbrains:1.0.0-beta-01")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ChessAgainstEngine"
            packageVersion = "1.0.0"
            description = "Play chess against the UCI engine you provide."
            vendor = "Laurent Bernabe"
            licenseFile.set(project.file("license.txt"))

            linux {
                appCategory = "Game"
                debMaintainer = "laurent.bernabe@gmail.com"
                iconFile.set(project.file("icon.png"))
            }

            windows {
                console = false
                iconFile.set(project.file("icon.ico"))
            }
        }
    }
}
