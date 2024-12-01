#FROM openjdk:11-jre
#ADD build/libs/process-7.0.2-beta1_cdk.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
FROM openjdk:11-jre
LABEL maintainer=pavel.stastny
WORKDIR /app
COPY build/install/cdkprocesses/lib lib/
COPY build/install/cdkprocesses/bin bin/
ENTRYPOINT ["bin/cdkprocesses"]

