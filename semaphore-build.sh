#!/bin/bash
set -e

function check() {
    ./gradlew check --scan --build-cache --configure-on-demand
    bash <(curl -s https://codecov.io/bash) -t $CODECOV_IO_TOKEN
}

function build() {
    echo "nexusUsername=$SONATYPE_USER" >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties

    ./gradlew build snapshot --scan --build-cache --configure-on-demand

    ./gradlew sonarqube \
            -Dsonar.login=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarqube.com \
            -Dsonar.organization=rahulsom-github

#    ./gradlew srcclr
}

if [ "$PULL_REQUEST_NUMBER" != "" ]; then
    check
elif [ "$BRANCH_NAME" = "master" ]; then
    build
elif [[ "$BRANCH_NAME" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
    build
else
    check
fi
