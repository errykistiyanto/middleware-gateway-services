FROM openjdk:11-jdk-slim
COPY target/eureka.jar opt/eureka.jar
ENV TZ Asia/Jakarta
ENTRYPOINT ["java","-jar","/opt/eureka.jar"]
LABEL maintainer="errykistiyanto@gmail.com"

# docker buildx build --platform linux/amd64,linux/arm64 --push -t dtqid/migs-eureka:latest .