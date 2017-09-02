#FROM gradle:4.0-jdk8-alpine
FROM openjdk:8-jdk-alpine

USER root

WORKDIR /usr/src/clustercode

ENV \
    CC_DEFAULT_DIR="/usr/src/clustercode/default" \
    CC_CONFIG_FILE="/usr/src/clustercode/config/clustercode.properties" \
    CC_CONFIG_DIR="/usr/src/clustercode/config" \
    CC_LOG_CONFIG_FILE="default/config/log4j2.xml" \
    JAVA_ARGS=""

ARG \
    GRADLE_VERSION="4.0.1"

VOLUME \
    /input \
    /output \
    /profiles \
    /var/tmp/clustercode \
    $CC_CONFIG_DIR

# Port 5005 is used for java remote debug, do not publish this port in production.
EXPOSE \
    7600/tcp 7600/udp \
    5005 \
    8080

CMD ["/usr/src/clustercode/docker-entrypoint.sh"]

RUN \
    mkdir /run/nginx && \
    apk update && \
    apk upgrade && \
    apk add --no-cache ffmpeg nginx supervisor

COPY docker/default.nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY \
    build.gradle \
    settings.gradle \
    package.json \
    .babelrc \
    docker/docker-entrypoint.sh docker/supervisord.conf ./
COPY webpack webpack
COPY webpackcfg webpackcfg
COPY docker/default default
COPY static static
COPY src src/
RUN \
    echo "Installing Gradle" && \
    apk add --no-cache unzip openssl && \
    wget -O gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" && \
    unzip -q gradle.zip && \
    rm gradle.zip && \
    echo "Building clustercode" && \
    gradle-${GRADLE_VERSION}/bin/gradle -w fullBuild generateSwaggerDocumentation && \
    mv build/libs/clustercode.jar clustercode.jar && \
    mv build/swagger/* static/ && \
    apk del unzip && \
    rm -r *gradle* && \
    rm -r .gradle && \
    rm -r ~/.gradle && \
    rm -r build
RUN \
    echo "Installing node and packages" && \
    apk add --no-cache nodejs-current-npm && \
    npm install --quiet --no-optional && \
    echo "Bulding clustercode-admin" && \
    npm run build && \
    echo "Cleaning up" && \
    apk del nodejs-current-npm && \
    rm -r ~/.npm && \
    rm -r node_modules && \
    rm -r package.json && \
    rm -r .babelrc && \
    rm -r webpack* && \
    rm -r static && \
    rm -r src
