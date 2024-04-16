FROM openjdk:17-jdk-alpine
MAINTAINER Skorobahatyi Mykola, nikolays2001@ukr.net
LABEL version="1.0"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
#ENTRYPOINT ["java", "-Dspring.profiles.active=mock", "-jar", "/app.jar"]