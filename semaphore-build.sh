#!/bin/bash

if [ "$BRANCH_NAME" = "master" ] && [ "$PULL_REQUEST_NUMBER" = "" ]; then

    echo "nexusUsername=$SONATYPE_USER" >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties

    ./gradlew build snapshot --scan --build-cache --configure-on-demand
    
    ./gradlew sonarqube \
            -Dsonar.login=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarqube.com \
            -Dsonar.organization=rahulsom-github

else
    ./gradlew check --scan --build-cache --configure-on-demand
fi