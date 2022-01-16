#!/usr/bin/env bash
set -e
source ./.docker/sonarQube/urlEncode.sh
source ./.docker/project/wait.sh
docker-compose -f ./.docker/sonarQube/docker-compose.yml up -d
wait_for_url "http://localhost:9000"
mvn clean verify sonar:sonar
project_location=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.groupId}:${project.artifactId}' --non-recursive exec:exec 2>/dev/null)
encode "$project_location" encoded_project_location
project_home="http://localhost:9000/dashboard?id="+$encoded_project_location
x-www-browser $project_home