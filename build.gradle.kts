plugins {
    java
}

group = "com.dynamictale"
version = "0.3.0-beta7.1"

repositories {
    mavenCentral()
    maven {
        name = "HytaleRelease"
        url = uri("https://maven.hytale.com/release")
    }
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.jar {
    archiveBaseName.set("DymanicTale")
    archiveVersion.set(project.version.toString())
}