#!/usr/bin/env bash

rm -rf docs
env JAVA_HOME="/usr/lib/jvm/java-14-openjdk" mvn clean compile javadoc:javadoc
cp -r target/site/apidocs ./docs