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
    ./gradlew --scan --build-cache --configure-on-demand "$@"
}

function codecov() {
    bash <(curl -s https://codecov.io/bash) -t ${CODECOV_IO_TOKEN}
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

function getLatestRelease() {
    curl -s "https://api.github.com/repos/$1/releases/latest" \
            | jq -r ".tag_name" \
            | sed -e "s/^v//g"
}

function main() {
    if [ "$SEMAPHORE_TRIGGER_SOURCE" = "scheduler" ]; then
        local NEW_GRADLE=$(getLatestRelease gradle/gradle)
        echo "Upgrading gradle to $NEW_GRADLE"
        gw wrapper --gradle-version ${NEW_GRADLE} --distribution-type all
        gw check && codecov
    else
        if [ "$PULL_REQUEST_NUMBER" != "" ]; then
            gw check &&  codecov
        elif [ "$BRANCH_NAME" = "master" ]; then
            setupSonatype && gw build snapshot && sonarqube
        elif [[ "$BRANCH_NAME" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
            setupSonatype && gw build snapshot && sonarqube
        else
            gw check && codecov
        fi
    fi
}

function setup() {
    mkdir -p ~/.gradle/cache
    rsync -a ${SEMAPHORE_CACHE_DIR}/gradle ~/.gradle/cache || echo "no cache existed"
    gw --scan --build-cache --configure-on-demand
}

function recache() {
    git status
    mkdir -p ${SEMAPHORE_CACHE_DIR}/gradle
    rsync -a ~/.gradle/cache ${SEMAPHORE_CACHE_DIR}/gradle
}

setup
main
recache