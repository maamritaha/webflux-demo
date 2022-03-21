FROM openjdk:17-alpine
MAINTAINER Taha <maamritaha@gmail.com>
RUN addgroup -g 1000 artistApp && \
    adduser -u 1000 -G artistApp -h /home/artistApp -D artistApp
USER artistApp:artistApp
#jar file path relative to docker compose context
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]