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
readonly GRGIT_USER
readonly SRC_CLR_TOKEN
readonly GH_TOKEN
readonly TERM

function gw() {
    ./gradlew "$@"
}

function sonarqube() {
    gw sonarqube -Dsonar.login=${SONAR_TOKEN}
}

function main() {
        if [[ "$BRANCH_NAME" = "master" ]]; then
            gw build  && sonarqube
        elif [[ "$BRANCH_NAME" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
            gw build  && sonarqube
        else
            gw check asciidoctor
        fi
}

main
