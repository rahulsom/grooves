#!/bin/bash
set -e

# Semaphore variables you get for free
readonly SEMAPHORE_TRIGGER_SOURCE
readonly PULL_REQUEST_NUMBER
readonly BRANCH_NAME
readonly SEMAPHORE_CACHE_DIR

# Secrets that are exported through build configuration
readonly CODECOV_IO_TOKEN
readonly SONAR_TOKEN
readonly SONATYPE_USER
readonly SONATYPE_PASSWORD
readonly GRGIT_USER
readonly SRC_CLR_TOKEN
readonly GH_TOKEN
readonly TERM

function gw() {
    ./gradlew "$@"
}

function setupSonatype() {
    echo "nexusUsername=$SONATYPE_USER"     >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties
}

function sonarqube() {
    gw sonarqube \
            -Dsonar.login=${SONAR_TOKEN} \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.organization=rahulsom-github
}

function main() {
        if [[ "$BRANCH_NAME" = "master" ]]; then
            setupSonatype && gw build snapshot && sonarqube
        elif [[ "$BRANCH_NAME" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
            setupSonatype && gw build snapshot && sonarqube
        else
            gw check asciidoctor
        fi
}

main
