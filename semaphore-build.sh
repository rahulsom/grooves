#!/bin/bash

function check() {
    ./gradlew check --scan --build-cache --configure-on-demand --continue
}

function build() {
    echo "nexusUsername=$SONATYPE_USER" >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties

    ./gradlew build snapshot \
        --scan \
        --build-cache \
        --configure-on-demand \
        --continue

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
