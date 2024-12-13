#FROM openjdk:11-jre
#ADD build/libs/process-7.0.2-beta1_cdk.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
FROM openjdk:11-jre
LABEL maintainer=pavel.stastny
WORKDIR /app
COPY build/install/cdkreharvest/lib lib/
COPY build/install/cdkreharvest/bin bin/
ENTRYPOINT ["bin/cdkreharvest"]

