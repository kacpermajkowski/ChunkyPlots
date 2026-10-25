#!/usr/bin/env bash
set -e

echo "=========================================================="
echo "  Setting up ChunkyPlots Dev Container & Paper Dev Server  "
echo "=========================================================="

WORKSPACE_DIR="$(pwd)"
DEV_SERVER_DIR="${DEV_SERVER_DIR:-${WORKSPACE_DIR}/dev-server}"
SCRIPTS_DIR="${WORKSPACE_DIR}/.devcontainer/scripts"

echo "==> Workspace:   ${WORKSPACE_DIR}"
echo "==> Dev Server:  ${DEV_SERVER_DIR}"

mkdir -p "${DEV_SERVER_DIR}/plugins"
mkdir -p "${DEV_SERVER_DIR}/logs"
mkdir -p "${DEV_SERVER_DIR}/config"

# 1. Download Paper 26.3 server jar if not present
PAPER_VERSION="26.3"
PAPER_JAR="${DEV_SERVER_DIR}/paper.jar"

if [ ! -f "${PAPER_JAR}" ]; then
    echo "==> Resolving latest Paper ${PAPER_VERSION} build from PaperMC API..."
    USER_AGENT="ChunkyPlotsDevContainer/1.0"
    BUILDS_JSON=$(curl -sSL -H "User-Agent: ${USER_AGENT}" "https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds" 2>/dev/null || true)

    LATEST_BUILD=$(echo "${BUILDS_JSON}" | jq -r 'map(select(.channel == "STABLE")) | .[0] | .id // empty' 2>/dev/null || true)
    if [ -z "${LATEST_BUILD}" ]; then
        LATEST_BUILD=$(echo "${BUILDS_JSON}" | jq -r '.[0] | .id // empty' 2>/dev/null || true)
    fi

    DOWNLOADED=false
    if [ -n "${LATEST_BUILD}" ]; then
        echo "==> Found Paper ${PAPER_VERSION} build #${LATEST_BUILD}"
        BUILD_INFO=$(curl -sSL -H "User-Agent: ${USER_AGENT}" "https://fill.papermc.io/v3/projects/paper/versions/${PAPER_VERSION}/builds/${LATEST_BUILD}" 2>/dev/null || true)
        DOWNLOAD_URL=$(echo "${BUILD_INFO}" | jq -r '.downloads."server:default".url // empty' 2>/dev/null || true)
        JAR_NAME=$(echo "${BUILD_INFO}" | jq -r '.downloads."server:default".name // "paper-'${PAPER_VERSION}'-'${LATEST_BUILD}'.jar"' 2>/dev/null || true)

        if [ -n "${DOWNLOAD_URL}" ]; then
            echo "==> Downloading Paper: ${JAR_NAME}..."
            if curl -sSL -H "User-Agent: ${USER_AGENT}" -o "${DEV_SERVER_DIR}/${JAR_NAME}" "${DOWNLOAD_URL}"; then
                ln -sf "${JAR_NAME}" "${PAPER_JAR}"
                DOWNLOADED=true
                echo "==> Downloaded Paper ${PAPER_VERSION} (#${LATEST_BUILD}) successfully."
            fi
        fi
    fi

    # Fallback to direct download if API resolution failed
    if [ "${DOWNLOADED}" = "false" ]; then
        echo "==> Downloading verified Paper 26.3 build..."
        FALLBACK_URL="https://fill-data.papermc.io/v1/objects/f649771a2190214306fb0cfba22c7b2cbac5df6308b1158309593603ed07d488/paper-26.3-222.jar"
        curl -sSL -H "User-Agent: ${USER_AGENT}" -o "${DEV_SERVER_DIR}/paper-26.3-222.jar" "${FALLBACK_URL}"
        ln -sf "paper-26.3-222.jar" "${PAPER_JAR}"
        echo "==> Downloaded Paper 26.3 build 222."
    fi
else
    echo "==> Paper server jar already present at ${PAPER_JAR}."
fi

# 2. Accept Minecraft EULA
echo "eula=true" > "${DEV_SERVER_DIR}/eula.txt"
echo "==> Minecraft EULA accepted."

# 3. Configure server.properties for local container development
if [ ! -f "${DEV_SERVER_DIR}/server.properties" ]; then
    cat << 'EOF' > "${DEV_SERVER_DIR}/server.properties"
# Minecraft server properties - ChunkyPlots Dev Container
accepts-transfers=false
allow-flight=true
broadcast-console-to-ops=true
broadcast-rcon-to-ops=true
difficulty=hard
enable-command-block=true
enable-rcon=true
enable-status=true
enforce-secure-profile=false
enforce-whitelist=false
gamemode=survival
hardcore=false
hide-online-players=false
level-name=world
level-type=minecraft\:normal
max-players=20
motd=ChunkyPlots Paper Dev Server
network-compression-threshold=256
online-mode=false
op-permission-level=4
player-idle-timeout=0
query.port=25565
rcon.password=localdevpassword
rcon.port=25575
server-ip=0.0.0.0
server-port=25565
simulation-distance=10
spawn-protection=0
sync-chunk-writes=true
use-native-transport=true
view-distance=10
white-list=false
EOF
    echo "==> Created dev-server/server.properties (online-mode=false, RCON=25575, Game=25565)."
else
    # Ensure RCON and ports are enabled even if server.properties existed
    sed -i 's/^enable-rcon=.*/enable-rcon=true/' "${DEV_SERVER_DIR}/server.properties" 2>/dev/null || true
    sed -i 's/^rcon.password=.*/rcon.password=localdevpassword/' "${DEV_SERVER_DIR}/server.properties" 2>/dev/null || true
    sed -i 's/^rcon.port=.*/rcon.port=25575/' "${DEV_SERVER_DIR}/server.properties" 2>/dev/null || true
    sed -i 's/^server-port=.*/server-port=25565/' "${DEV_SERVER_DIR}/server.properties" 2>/dev/null || true
fi

# 4. Install companion / soft-dependency plugins
if [ ! -f "${DEV_SERVER_DIR}/plugins/Vault.jar" ] || [ ! -s "${DEV_SERVER_DIR}/plugins/Vault.jar" ]; then
    echo "==> Downloading Vault plugin (soft dependency)..."
    curl -sSL -o "${DEV_SERVER_DIR}/plugins/Vault.jar" "https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar" || true
fi

if [ ! -f "${DEV_SERVER_DIR}/plugins/PlugManX.jar" ] || [ ! -s "${DEV_SERVER_DIR}/plugins/PlugManX.jar" ] || [ $(wc -c < "${DEV_SERVER_DIR}/plugins/PlugManX.jar") -lt 1000 ]; then
    echo "==> Downloading PlugManX plugin (hot reload)..."
    curl -sSL -o "${DEV_SERVER_DIR}/plugins/PlugManX.jar" "https://cdn.modrinth.com/data/yro4niHu/versions/j22EUw2Y/PlugManX-3.2.1.jar" || true
fi

# 5. Configure deploy.properties for container environment
CONTAINER_DEPLOY_PROPS="${WORKSPACE_DIR}/deploy.properties"
if [ ! -f "${CONTAINER_DEPLOY_PROPS}" ] || grep -qE ':[\\/]' "${CONTAINER_DEPLOY_PROPS}" 2>/dev/null; then
    if [ -f "${CONTAINER_DEPLOY_PROPS}" ]; then
        cp "${CONTAINER_DEPLOY_PROPS}" "${WORKSPACE_DIR}/deploy.properties.host.bak" 2>/dev/null || true
    fi
    cat << EOF > "${CONTAINER_DEPLOY_PROPS}"
# ============================================================
#  ChunkyPlots - Container Dev Server Configuration
# ============================================================
devserver.dir=${DEV_SERVER_DIR}
devserver.plugins.dir=${DEV_SERVER_DIR}/plugins
devserver.jar.target=${DEV_SERVER_DIR}/plugins/ChunkyPlots.jar

rcon.host=127.0.0.1
rcon.port=25575
rcon.password=localdevpassword

deploy.jar.target=${DEV_SERVER_DIR}/plugins/ChunkyPlots.jar
EOF
    echo "==> Configured deploy.properties for dev container paths."
fi

# 6. Copy RCON helper script into dev-server
cp "${SCRIPTS_DIR}/rcon.py" "${DEV_SERVER_DIR}/rcon.py" 2>/dev/null || true
chmod +x "${DEV_SERVER_DIR}/rcon.py" 2>/dev/null || true

# 7. Create server launcher and management scripts
cat << 'EOF' > "${DEV_SERVER_DIR}/start.sh"
#!/usr/bin/env bash
DEV_SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DEV_SERVER_DIR}"

echo "=========================================================="
echo "  Starting ChunkyPlots Paper Dev Server (Minecraft 26.3)  "
echo "  * Game Port: 25565  (Connect locally: localhost:25565)  "
echo "  * RCON Port: 25575  (Password: localdevpassword)        "
echo "  * JVM Debug: 5005   (Attach VS Code debugger)          "
echo "=========================================================="

exec java \
    -Xms2G -Xmx2G \
    -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
    -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch \
    -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
    -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 \
    -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 \
    -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem \
    -XX:MaxTenuringThreshold=1 \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
    -jar paper.jar --nogui "$@"
EOF

cat << 'EOF' > "${DEV_SERVER_DIR}/start-bg.sh"
#!/usr/bin/env bash
DEV_SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${DEV_SERVER_DIR}"

if tmux has-session -t paper-server 2>/dev/null; then
    echo "==> Paper Dev Server is already running in tmux session 'paper-server'."
    echo "==> Run '${DEV_SERVER_DIR}/attach.sh' to view server console."
    exit 0
fi

echo "==> Starting Paper Dev Server in background tmux session 'paper-server'..."
tmux new-session -d -s paper-server "bash ${DEV_SERVER_DIR}/start.sh"
echo "==> Server started in background!"
echo "==> Run '${DEV_SERVER_DIR}/attach.sh' to view console."
echo "==> Forwarded ports: 25565 (Game), 25575 (RCON), 5005 (Debug)."
EOF

cat << 'EOF' > "${DEV_SERVER_DIR}/attach.sh"
#!/usr/bin/env bash
if ! tmux has-session -t paper-server 2>/dev/null; then
    echo "==> Paper server tmux session is not running."
    echo "==> Start it with ./dev-server/start-bg.sh or ./dev-server/start.sh"
    exit 1
fi
echo "==> Attaching to Paper console. Press Ctrl+B then D to detach without stopping server."
tmux attach-session -t paper-server
EOF

cat << 'EOF' > "${DEV_SERVER_DIR}/stop.sh"
#!/usr/bin/env bash
DEV_SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "==> Stopping Paper Dev Server..."
if [ -f "${DEV_SERVER_DIR}/rcon.py" ]; then
    python3 "${DEV_SERVER_DIR}/rcon.py" "stop" 2>/dev/null || true
fi
sleep 2
if tmux has-session -t paper-server 2>/dev/null; then
    tmux kill-session -t paper-server 2>/dev/null || true
fi
echo "==> Paper Dev Server stopped."
EOF

cat << 'EOF' > "${DEV_SERVER_DIR}/rcon.sh"
#!/usr/bin/env bash
DEV_SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
python3 "${DEV_SERVER_DIR}/rcon.py" "$@"
EOF

cat << 'EOF' > "${DEV_SERVER_DIR}/hot-reload.sh"
#!/usr/bin/env bash
WORKSPACE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${WORKSPACE_DIR}"
echo "==> Compiling and hot-reloading ChunkyPlots plugin..."
gradle hotDeploy --no-daemon
EOF

chmod +x "${DEV_SERVER_DIR}"/*.sh

# 8. Compile and deploy ChunkyPlots plugin
echo "==> Compiling ChunkyPlots plugin..."
cd "${WORKSPACE_DIR}"
gradle jar --no-daemon

echo "==> Deploying ChunkyPlots JAR to dev-server..."
gradle deployJar --no-daemon
echo "==> ChunkyPlots.jar deployed to ${DEV_SERVER_DIR}/plugins/ChunkyPlots.jar"

echo "=========================================================="
echo "  ChunkyPlots Dev Environment Ready!                      "
echo "                                                          "
echo "  Server Directory: dev-server/                           "
echo "  Plugin Installed: dev-server/plugins/ChunkyPlots.jar    "
echo "                                                          "
echo "  To start server interactively:                          "
echo "    ./dev-server/start.sh                                 "
echo "                                                          "
echo "  To start server in background (tmux):                   "
echo "    ./dev-server/start-bg.sh                              "
echo "    ./dev-server/attach.sh                                "
echo "                                                          "
echo "  To hot-reload after code changes:                       "
echo "    gradle hotDeploy    or   ./dev-server/hot-reload.sh   "
echo "                                                          "
echo "  Forwarded Ports:                                        "
echo "    - 25565: Minecraft client (connect to localhost:25565)"
echo "    - 25575: Paper RCON                                   "
echo "    - 5005:  Remote JVM debugger                          "
echo "=========================================================="

