#!/bin/bash

if [[ ${CC_LOG_LEVEL} == "debug" ]]; then
    echo "Invoking java ${JVM_ARGS} -jar /opt/clustercode.jar ${@}"
fi

exec java ${JVM_ARGS} -jar /opt/clustercode.jar ${@}
