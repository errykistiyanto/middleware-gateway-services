FROM openjdk:11-jdk-slim
COPY target/openapi.jar opt/openapi.jar
COPY deploy /deploy
COPY packager /packager
COPY log /log
ENV TZ Asia/Jakarta
ENTRYPOINT ["java","-jar","/opt/finnet-svc.jar"]
LABEL maintainer="errykistiyanto@gmail.com"

#docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-finnet-svc:latest .