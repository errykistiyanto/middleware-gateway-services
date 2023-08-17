#!/bin/bash
mvn clean install -DskipTests
mvn clean package docker:build -DskipTests
docker tag dtqid/migs-eureka:latest dtqid/migs-eureka:1.2.0
docker push dtqid/migs-eureka:1.2.0
docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-eureka:1.2.0 .