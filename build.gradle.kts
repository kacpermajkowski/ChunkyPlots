import java.util.Properties

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ChunkyPlots"
version = "DEV-2026.1"

// Load deploy.properties
val deployProps = Properties()
val deployFile = rootProject.file("deploy.properties")
if (deployFile.exists()) deployProps.load(deployFile.inputStream())

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // NMS via paperweight (replaces paper-nms + paper-api)
    paperweight.paperDevBundle("26.1.2.build.+")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    // Apache Commons
    implementation("org.apache.commons:commons-lang3:3.20.0")

    // Tests
    testImplementation("junit:junit:4.13.2")
}

paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.processResources {
    filesMatching(listOf("**/*.yml", "**/*.yaml", "**/*.json")) {
        expand(project.properties)
    }
}

// Copy JAR to dev server (equivalent of maven-antrun copy-jar-to-dev-server)
tasks.register<Copy>("deployJar") {
    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile)
    into(file(deployProps.getProperty("deploy.jar.target", "").substringBeforeLast("/")))
    rename { deployProps.getProperty("deploy.jar.target", it).substringAfterLast("/") }
}

// RCON restart (equivalent of exec-maven-plugin rcon-restart)
tasks.register<JavaExec>("rconRestart") {
    dependsOn("deployJar")
    classpath = configurations.detachedConfiguration(
        dependencies.create("com.github.t9t.minecraft-rcon-client:minecraft-rcon-client:1.0.0")
    )
    mainClass.set("com.github.t9t.minecraftrconclient.RconClientCli")
    args = listOf(
        "${deployProps["rcon.host"]}:${deployProps["rcon.port"]}",
        deployProps.getProperty("rcon.password", ""),
        "spigot:restart"
    )
}

// Combined deploy task (mirrors `mvn deploy`)
tasks.register("deploy") {
    dependsOn("deployJar", "rconRestart")
}