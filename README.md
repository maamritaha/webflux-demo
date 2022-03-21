# Artist project

this project is a spring webflux demo

### Project build and start
this project can be started in a local machine or using docker containers

#### With local machine
to start the project using a local machine :
* make sure that [postgres](https://www.postgresql.org/) is installed
* adapt .env liquibase.properties and application.yml files with the postgres configuration
* run command [./mvnw clean package]
* run command [./mvnw liquibase:update] 
* run command [java -jar ./target/*.jar]
#### with docker
* make sure that [docker](https://www.docker.com/) engine and compose are installed
* make sure entry-point.sh , ./.docker/util/wait.sh and ./.docker/util/urlEncode.sh are executable
* run command [./entry-point.sh]

### Other Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.6/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.6/maven-plugin/reference/html/#build-image)
* [Spring Data R2DBC](https://docs.spring.io/spring-boot/docs/2.5.6/reference/html/spring-boot-features.html#boot-features-r2dbc)

### Guides
The following guides illustrate how to use some features concretely:

* [Acessing data with R2DBC](https://spring.io/guides/gs/accessing-data-r2dbc/)

### Additional Links
These additional references should also help you:

* [R2DBC Homepage](https://r2dbc.io)

## Missing R2DBC Driver

Make sure to include a [R2DBC Driver](https://r2dbc.io/drivers/) to connect to your database.
