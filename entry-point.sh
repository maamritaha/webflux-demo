#!/usr/bin/env bash
set -e
source .env.dev
source ./.docker/util/wait.sh
Green='\033[0;32m'
ColorReset='\033[0m'
echo -e "${Green}build java project${ColorReset}"
./mvnw clean package -T 4C
echo -e "${Green}docker clean up${ColorReset}"
docker-compose -f ./.docker/project/docker-compose.yml down -v
echo -e "${Green}starting project${ColorReset}"
docker-compose -f ./.docker/project/docker-compose.yml --env-file .env.dev up -d db
wait_for_command "docker-compose -f ./.docker/project/docker-compose.yml exec db psql \
                                                    'host=${POSTGRES_HOST} \
                                                     port=${POSTGRES_PORT} \
                                                     dbname=${POSTGRES_DB} \
                                                     user=${POSTGRES_USER} \
                                                     password=${POSTGRES_PASSWORD}' \
                                                     -c 'select 1'"
docker-compose -f ./.docker/project/docker-compose.yml --env-file .env.dev up -d liquibase adminer
wait_for_command "docker-compose -f ./.docker/project/docker-compose.yml exec db psql \
                                                    'host=${POSTGRES_HOST} \
                                                     port=${POSTGRES_PORT} \
                                                     dbname=${POSTGRES_DB} \
                                                     user=${POSTGRES_USER} \
                                                     password=${POSTGRES_PASSWORD}' \
                                                     -c 'select 1 from ${TABLE_NAME}'"
docker-compose -f ./.docker/project/docker-compose.yml --env-file .env.dev up --build -d
wait_for_url "http://localhost:8080/actuator/health"
echo -e "${Green}project started${ColorReset}"
x-www-browser http://localhost:8080/actuator/health
