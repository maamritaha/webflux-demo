FROM openjdk:11-jre-slim-buster
MAINTAINER Taha <maamritaha@gmail.com>
RUN apt-get update && \
    apt-get update -y && apt-get install -y lsof && \
    apt-get install -y net-tools
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
RUN groupadd --gid 5000 artistApp && \
    useradd --home-dir /home/default --create-home --uid 5000 --gid 5000 --shell /bin/sh --skel /dev/null artistApp
USER artistApp:artistApp
#jar file path relative to docker compose context
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} /app/app.jar
WORKDIR /app
ENTRYPOINT ["java","-jar","app.jar"]