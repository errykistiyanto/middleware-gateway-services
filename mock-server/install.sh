#!/bin/bash
mvn clean install -DskipTests
mvn clean package docker:build -DskipTests
docker tag dtqid/migs-mock-server:latest dtqid/migs-mock-server:1.2.0
docker push dtqid/migs-mock-server:1.2.0
docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-mock-server:1.2.0 .