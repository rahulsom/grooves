#!/bin/bash
set -e

# Semaphore variables you get for free
readonly SEMAPHORE_TRIGGER_SOURCE
readonly PULL_REQUEST_NUMBER
readonly BRANCH_NAME

# Secrets that are exported through build configuration
readonly CODECOV_IO_TOKEN
readonly SONAR_TOKEN
readonly SONATYPE_USER
readonly SONATYPE_PASSWORD

function gw(){
    ./gradlew --scan --build-cache --configure-on-demand "$@"
}

function check() {
    gw check
    bash <(curl -s https://codecov.io/bash) -t ${CODECOV_IO_TOKEN}
}

function build() {
    echo "nexusUsername=$SONATYPE_USER" >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties

    gw build snapshot

    gw sonarqube \
            -Dsonar.login=${SONAR_TOKEN} \
            -Dsonar.host.url=https://sonarcloud.io \
            -Dsonar.organization=rahulsom-github
}

get_latest_release() {
  curl -s "https://api.github.com/repos/$1/releases/latest" \
        | jq -r ".tag_name" \
        | sed -e "s/^v//g"
}

if [ "$SEMAPHORE_TRIGGER_SOURCE" = "scheduler" ]; then
    gw wrapper --gradle-version $(get_latest_release gradle/gradle) --distribution-type all
    check
else
    if [ "$PULL_REQUEST_NUMBER" != "" ]; then
        check
    elif [ "$BRANCH_NAME" = "master" ]; then
        build
    elif [[ "$BRANCH_NAME" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
        build
    else
        check
    fi
fi

