FROM mcr.microsoft.com/devcontainers/java:25

# Switch to root to install packages
USER root

# Install essential development and networking tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    wget \
    jq \
    unzip \
    tar \
    git \
    bash \
    tmux \
    procps \
    netcat-openbsd \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Install Gradle 9.6.0
ARG GRADLE_VERSION=9.6.0
RUN mkdir -p /opt/gradle \
    && curl -sSL -o /tmp/gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    && unzip -q /tmp/gradle.zip -d /opt/gradle \
    && rm /tmp/gradle.zip \
    && ln -s /opt/gradle/gradle-${GRADLE_VERSION}/bin/gradle /usr/local/bin/gradle

# Install Apache Maven 3.9.9
ARG MAVEN_VERSION=3.9.9
RUN mkdir -p /opt/maven \
    && curl -sSL -o /tmp/maven.tar.gz "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && tar -xzf /tmp/maven.tar.gz -C /opt/maven \
    && rm /tmp/maven.tar.gz \
    && ln -s /opt/maven/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn

# Set environment variables
ENV GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
ENV MAVEN_HOME=/opt/maven/apache-maven-${MAVEN_VERSION}
ENV PATH="${GRADLE_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"

# Setup compatibility directory/symlink for legacy path /home/strongest-warrior/dev-projects/dev-server
RUN mkdir -p /home/strongest-warrior/dev-projects \
    && ln -sf /workspaces/chunkyplots/dev-server /home/strongest-warrior/dev-projects/dev-server \
    && (chown -R vscode:vscode /home/strongest-warrior 2>/dev/null || true)

# Switch back to non-root developer user
USER vscode

