import java.util.Properties
import java.net.Socket
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "ChunkyPlots"
version = "DEV-2026.3"

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
    paperweight.paperDevBundle("26.3.build.+")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    // Apache Commons
    implementation("org.apache.commons:commons-lang3:3.20.0")

    // Database: SQLite & HikariCP
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Tests
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
}

paperweight.reobfArtifactConfiguration =
    io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "2g"
    jvmArgs("-Dnet.bytebuddy.experimental=true")
}

tasks.processResources {
    filesMatching(listOf("**/*.yml", "**/*.yaml", "**/*.json")) {
        expand(project.properties)
    }
}

fun sendRcon(host: String, port: Int, pass: String, command: String): String {
    Socket(host, port).use { socket ->
        socket.soTimeout = 10000
        val output = DataOutputStream(socket.getOutputStream())
        val input = DataInputStream(socket.getInputStream())

        fun sendPacket(id: Int, type: Int, payload: String) {
            val body = payload.toByteArray(Charsets.UTF_8)
            val length = 4 + 4 + body.size + 2
            val buf = ByteBuffer.allocate(4 + length).order(ByteOrder.LITTLE_ENDIAN)
            buf.putInt(length)
            buf.putInt(id)
            buf.putInt(type)
            buf.put(body)
            buf.put(0.toByte())
            buf.put(0.toByte())
            output.write(buf.array())
            output.flush()
        }

        fun readPacket(): Pair<Int, String> {
            val lenBytes = ByteArray(4)
            input.readFully(lenBytes)
            val length = ByteBuffer.wrap(lenBytes).order(ByteOrder.LITTLE_ENDIAN).int
            val payload = ByteArray(length)
            input.readFully(payload)
            val buf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
            val id = buf.int
            val type = buf.int
            val body = if (length > 10) String(payload, 8, length - 10, Charsets.UTF_8) else ""
            return id to body
        }

        // Login packet (type 3)
        sendPacket(1, 3, pass)
        val (authId, _) = readPacket()
        if (authId == -1) {
            throw RuntimeException("RCON authentication failed (invalid password)")
        }

        // Command packet (type 2)
        sendPacket(2, 2, command)
        val (_, response) = readPacket()
        return response
    }
}

// Copy JAR to dev server
tasks.register<Copy>("deployJar") {
    dependsOn(tasks.jar)
    group = "deployment"
    from(tasks.jar.get().archiveFile)
    into(file(deployProps.getProperty("deploy.jar.target", "/home/strongest-warrior/dev-projects/dev-server/plugins/ChunkyPlots.jar")).parentFile)
    rename { "ChunkyPlots.jar" }
}

// Hot-deploy: packages and hot-loads onto dev server via PlugMan without server reload/restart
tasks.register("hotDeploy") {
    dependsOn(tasks.jar)
    group = "deployment"
    description = "Packages and hot-loads ChunkyPlots onto the running dev server without restarting or /reload"

    doLast {
        val targetPath = deployProps.getProperty("deploy.jar.target", "/home/strongest-warrior/dev-projects/dev-server/plugins/ChunkyPlots.jar")
        val targetFile = file(targetPath)
        val sourceJar = tasks.jar.get().archiveFile.get().asFile

        targetFile.parentFile.mkdirs()
        sourceJar.copyTo(targetFile, overwrite = true)
        logger.lifecycle("==> Packaged & copied ${sourceJar.name} to ${targetFile.absolutePath}")

        val rconHost = deployProps.getProperty("rcon.host", "127.0.0.1")
        val rconPort = deployProps.getProperty("rcon.port", "25575").toInt()
        val rconPass = deployProps.getProperty("rcon.password", "devpass")

        try {
            logger.lifecycle("==> Connecting to Dev Server RCON ($rconHost:$rconPort)...")
            var response = sendRcon(rconHost, rconPort, rconPass, "plugman reload ChunkyPlots")
            if (response.contains("is not loaded", ignoreCase = true) || response.contains("unknown", ignoreCase = true)) {
                response = sendRcon(rconHost, rconPort, rconPass, "plugman load ChunkyPlots")
            }
            logger.lifecycle("==> [Dev Server Hot-Reload Response]:")
            logger.lifecycle(response.trim())
            logger.lifecycle("==> Successfully hot-loaded ChunkyPlots without server restart or reload!")
        } catch (e: java.net.ConnectException) {
            logger.lifecycle("==> Dev server is currently stopped ($rconHost:$rconPort).")
            logger.lifecycle("==> Plugin jar is deployed and ready for server boot.")
        } catch (e: Exception) {
            logger.warn("==> RCON notification: ${e.message}")
        }
    }
}

// Deploy alias
tasks.register("deploy") {
    group = "deployment"
    description = "Alias for hotDeploy"
    dependsOn("hotDeploy")
}

// Run Dev Server task
tasks.register<Exec>("runDevServer") {
    dependsOn("deployJar")
    group = "development"
    description = "Deploys ChunkyPlots and launches the Paper 26.3 dev server"
    val serverDir = file(deployProps.getProperty("devserver.dir", "/home/strongest-warrior/dev-projects/dev-server"))
    workingDir = serverDir
    commandLine("java", "-Xms2G", "-Xmx2G", "-jar", "paper.jar", "--nogui")
    standardInput = System.`in`
}
