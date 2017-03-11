#!/usr/bin/env bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  if [ "$TRAVIS_BRANCH" = "master" ]; then
    echo "nexusUsername=$SONATYPE_USER" >> gradle.properties
    echo "nexusPassword=$SONATYPE_PASSWORD" >> gradle.properties
    ./gradlew build uploadArchives
  else
    ./gradlew check
  fi
else
  ./gradlew check
fi
