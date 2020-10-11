#!/usr/bin/env bash

rm -rf docs
./mvnw clean compile javadoc:javadoc
cp -r target/site/apidocs ./docs