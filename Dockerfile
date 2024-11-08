###################################################
# Stage: base
# 
# This base stage ensures all other stages are using the same base image
# and provides common configuration for all stages, such as the working dir.
###################################################

## Take the ECLIPSE TEMURIN Image as base image (it includes JDK)
FROM eclipse-temurin:17.0.13_11-jre-focal AS base

## create a directory for our application code
WORKDIR /app

###################################################
################  BACKEND STAGES  #################
###################################################

###################################################
# Stage: backend-base
#
# This stage is used as the base for the backend-dev and test stages, since
# there are common steps needed for each.
###################################################
FROM base AS backend-dev

## COPY .mvn/ (LOCAL) to /app/.mvn (CONTAINER)
COPY .mvn/ ./.mvn

## COPY mvnw pom.xml to /app
COPY mvnw pom.xml ./

## resolve all dependencies
RUN ./mvnw dependency:go-offline

## COPY src (LOCAL) to /app/src (CONTAINER)
COPY src ./src

CMD ["./mvnw", "spring-boot:run"]
