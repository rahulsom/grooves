#!/usr/bin/env bash
set -e

if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  if [ "$TRAVIS_BRANCH" = "master" ]; then
    ./gradlew publish
  else
    ./gradlew check
  fi
else
  ./gradlew check
fi
