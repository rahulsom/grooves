#!/usr/bin/env bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  if [ "$TRAVIS_BRANCH" = "master" ]; then
    echo "nexusUsername=$SONATYPE_USER" >> ~/.gradle/gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> ~/.gradle/gradle.properties
    ./gradlew build --scan --configure-on-demand
    ./gradlew sonarqube \
            -Dsonar.login=$SONAR_TOKEN \
            -Dsonar.host.url=https://sonarqube.com \
            -Dsonar.organization=rahulsom-github \
            --stacktrace --configure-on-demand
    ./gradlew uploadArchives --scan --configure-on-demand
  else
    ./gradlew check --scan --configure-on-demand
  fi
else
  ./gradlew check --scan --configure-on-demand
fi
