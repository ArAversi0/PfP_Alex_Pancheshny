plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
}

application {
    mainClass = "com.pfp.desktop.PfpDesktopLauncher"
}

javafx {
    version = "17.0.15"
    modules = listOf("javafx.controls", "javafx.media")
}

dependencies {
    implementation(project(":libs:game-rules"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

val desktopPackageName = "PfP Companion"
val desktopAppVersion = "0.1.0"
val jpackageExecutable = providers.provider {
    val executableName = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "jpackage.exe"
    } else {
        "jpackage"
    }
    file("${System.getProperty("java.home")}/bin/$executableName").absolutePath
}
val jpackageInputDir = layout.buildDirectory.dir("jpackage/input")
val jpackageImageDir = layout.buildDirectory.dir("jpackage/image")
val jpackageInstallerDir = layout.buildDirectory.dir("jpackage/installer")

tasks.register<Sync>("prepareDesktopPackageInput") {
    dependsOn(tasks.named("jar"))
    into(jpackageInputDir)
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    from(configurations.runtimeClasspath)
}

tasks.register<Exec>("packageDesktopAppImage") {
    group = "distribution"
    description = "Builds a self-contained PfP Companion desktop app image with the bundled Java runtime."
    dependsOn("prepareDesktopPackageInput")

    doFirst {
        delete(jpackageImageDir)
    }

    commandLine(
        jpackageExecutable.get(),
        "--type", "app-image",
        "--name", desktopPackageName,
        "--app-version", desktopAppVersion,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--dest", jpackageImageDir.get().asFile.absolutePath,
        "--main-jar", tasks.named<Jar>("jar").get().archiveFileName.get(),
        "--main-class", application.mainClass.get()
    )
}

tasks.register<Exec>("packageDesktopInstaller") {
    group = "distribution"
    description = "Builds a Windows installer for PfP Companion. On Windows, jpackage requires WiX Toolset."
    dependsOn("prepareDesktopPackageInput")

    val installerType = if (System.getProperty("os.name").lowercase().contains("windows")) {
        "exe"
    } else {
        "app-image"
    }

    doFirst {
        delete(jpackageInstallerDir)
        if (installerType == "exe") {
            val pathDirectories = System.getenv("PATH")
                ?.split(File.pathSeparator)
                ?.map(::file)
                ?: emptyList()
            val hasWixTools = pathDirectories.any { directory ->
                directory.resolve("candle.exe").isFile && directory.resolve("light.exe").isFile
            }
            if (!hasWixTools) {
                throw GradleException(
                    "WiX Toolset is required to build a Windows installer. " +
                        "Install WiX 3.0 or later from https://wixtoolset.org and add candle.exe/light.exe to PATH. " +
                        "Use :apps:desktop:packageDesktopAppImage for a self-contained app folder without WiX."
                )
            }
        }
    }

    commandLine(
        jpackageExecutable.get(),
        "--type", installerType,
        "--name", desktopPackageName,
        "--app-version", desktopAppVersion,
        "--input", jpackageInputDir.get().asFile.absolutePath,
        "--dest", jpackageInstallerDir.get().asFile.absolutePath,
        "--main-jar", tasks.named<Jar>("jar").get().archiveFileName.get(),
        "--main-class", application.mainClass.get(),
        "--win-menu",
        "--win-shortcut"
    )
}
