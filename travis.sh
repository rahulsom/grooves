#!/usr/bin/env bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  if [ "$TRAVIS_BRANCH" = "master" ]; then
    echo "nexusUsername=$SONATYPE_USER" >> gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> gradle.properties
    ./gradlew build uploadArchives --scan --parallel --build-cache --configure-on-demand
    ./gradlew sonarqube -Dsonar.login=$SONAR_TOKEN -Dsonar.host.url=https://sonarqube.com -Dsonar.organization=rahulsom-github
  else
    ./gradlew check --scan --parallel --build-cache --configure-on-demand
  fi
else
  ./gradlew check --scan --parallel --build-cache --configure-on-demand
fi
