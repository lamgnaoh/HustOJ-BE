#
# Build stage
#
FROM maven:3.8.3-openjdk-17 AS build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

#
# Package stage
#
FROM eclipse-temurin:17-jdk-alpine
ARG TARGET=/usr/app/target
COPY --from=build $TARGET /app
EXPOSE 8080
ENTRYPOINT java -jar /app/*.jar