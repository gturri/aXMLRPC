#!/bin/bash -xe

rm -rf app/src/test app/src/main/java
cp -r ../src/test app/src
cp -r ../src/main/java app/src/main

./gradlew clean test
