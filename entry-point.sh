#!/usr/bin/env bash
set -e
source .env
green='\033[0;32m'
NC='\033[0m' # No Color
echo -e "${green}build java project${NC}"
./mvnw clean package
echo -e "${green}docker clean up${NC}"
docker-compose down -v
echo -e "${green}starting project${NC}"
docker-compose up -d db
./docker/wait-until.sh "docker-compose exec db psql 'host=${POSTGRES_HOST} \
                                                     port=${POSTGRES_PORT} \
                                                     dbname=${POSTGRES_DB} \
                                                     user=${POSTGRES_USER} \
                                                     password=${POSTGRES_PASSWORD}' \
                                                     -c 'select 1'"
docker-compose up -d liquibase adminer
./docker/wait-until.sh "docker-compose exec db psql 'host=${POSTGRES_HOST} \
                                                     port=${POSTGRES_PORT} \
                                                     dbname=${POSTGRES_DB} \
                                                     user=${POSTGRES_USER} \
                                                     password=${POSTGRES_PASSWORD}' \
                                                     -c 'select 1 from ${TABLE_NAME}'"
docker-compose up --build -d
./docker/wait-until.sh "curl -v http://localhost:8080/artist/all"
echo -e "${green}project started${NC}"
