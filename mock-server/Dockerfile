FROM openjdk:11-jdk-slim
COPY target/mock-server.jar opt/mock-server.jar
COPY log /log
ENV TZ Asia/Jakarta
ENTRYPOINT ["java","-jar","/opt/mock-server.jar"]
LABEL maintainer="errykistiyanto@gmail.com"

#docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-mock-server:latest .