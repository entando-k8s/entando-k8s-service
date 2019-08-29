FROM openjdk:8-jdk-alpine
MAINTAINER Sergio Marcelino <s.marcelino@entando.com>

COPY target/generated-artifact.jar app.jar
CMD ["java", "-jar", "app.jar"]