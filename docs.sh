#!/usr/bin/env bash

rm -rf docs
mvn clean compile javadoc:javadoc
cp -r target/site/apidocs ./docs