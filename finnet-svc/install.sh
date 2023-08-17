#!/bin/bash
mvn clean install -DskipTests
mvn clean package docker:build -DskipTests
docker tag itdevbankdki/finnet-svc:latest itdevbankdki/finnet-svc:1.2.0-iso-dev1
docker push itdevbankdki/finnet-svc:1.2.0-iso-dev1
docker buildx build --platform linux/amd64,linux/arm64 --push -t itdevbankdki/finnet-svc:1.2.0-iso-dev1 .

#docker buildx create --name mybuilder
#docker buildx use mybuilder
#docker buildx inspect --bootstrap