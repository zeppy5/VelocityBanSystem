plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "de.zeppy5"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://nexus.velocitypowered.com/repository/maven-public/")
}

dependencies {
    implementation("dev.dejvokep:boosted-yaml:1.3.6")
    implementation("mysql:mysql-connector-java:8.0.28")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
}

tasks {
    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("VelocityBanSystem-shadow")
        relocate("dev.dejvokep", "de.zeppy5.bansystem.libs.dejvokep")
    }
}