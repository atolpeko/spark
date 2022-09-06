FROM --platform=linux/x86_64 amazoncorretto:11-alpine-jdk
MAINTAINER atolpeko <alextolpeko@gmail.com>

ARG JAR_FILE
COPY ${JAR_FILE} user-service.jar

ENTRYPOINT ["java", "-jar", "/user-service.jar"]
