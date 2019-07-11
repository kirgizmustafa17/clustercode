#______________________________________________________________________________
#### Builder Image
ARG ARCH
FROM openjdk:11-jdk as builder

WORKDIR /usr/local/src/clustercode

RUN \
    apt-get update && \
    apt-get install tree

COPY ["gradle", "./gradle"]
COPY ["gradlew", "./"]

RUN \
    # The git versioning pluging requires it, otherwise fails.
    mkdir .git && \
    # Download gradle 5
    ./gradlew

COPY ["build.gradle", "settings.gradle", "./"]
RUN \
    # Download dependencies
    ./gradlew resolveDependencies

COPY .git ./.git
COPY src ./src

RUN \
    ./gradlew shadowJar

#______________________________________________________________________________
#### Runtime Image
ARG ARCH
FROM multiarch/alpine:${ARCH} as runtime

ENTRYPOINT ["clustercode"]

EXPOSE \
    8080/tcp

ARG TGT_DIR="/opt"
WORKDIR $TGT_DIR

RUN \
    apk update && \
    apk upgrade && \
    apk add --no-cache openjdk11-jre ffmpeg nano curl bash

COPY docker/clustercode.sh /bin/clustercode
COPY docker/default ${TGT_DIR}/default/

RUN \
    # Let's create the directories first so we can apply the permissions:
    mkdir -m 664 /input /output /profiles /var/tmp/clustercode

VOLUME \
    /input \
    /output \
    /profiles \
    /var/tmp/clustercode

ENV \
    JVM_ARGS="-XX:MaxRAMPercentage=70"

COPY --from=builder /usr/local/src/clustercode/build/libs/clustercode.jar ${TGT_DIR}/
USER 1001
