FROM openjdk:11-jdk-slim
COPY target/finnet-svc.jar opt/finnet-svc.jar
COPY log /log
ENV TZ Asia/Jakarta
ENTRYPOINT ["java","-jar","/opt/finnet-svc.jar"]
LABEL maintainer="errykistiyanto@gmail.com"

#docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-finnet-svc:latest .