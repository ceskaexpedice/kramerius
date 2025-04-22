FROM eclipse-temurin:21-jre
LABEL maintainer=pavel.stastny

WORKDIR /app

COPY build/install/cdkreharvest/lib lib/
COPY build/install/cdkreharvest/bin bin/

ENTRYPOINT ["bin/cdkreharvest"]

