#!/bin/bash

echoHelp()
{
  echo "Usage:"
  echo "    $0 [option]"
  echo "Options:"
  echo "    -h --help        Print Help Messages"
  echo "    --daemon         Startup program as daemon mode"
  echo ""
}

# java command
cmd="/usr/lib/jvm/java-16/bin/java"

if [ "x$cmd" = "x" ]; then
  cmd=`type -p java`
fi

if [ "x$cmd" = "x" ]; then
  if [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    cmd="$JAVA_HOME/bin/java"
  else
    echo "Error: missing java"
    exit 1
  fi
  # check java version
  jvm_version=$("$cmd" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if [[ "$jvm_version" < "16" ]]; then
    echo "Error: java16 is required."
    exit 1
  fi
fi

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
cd ${PRGDIR}


# JDK Options

# Memory
MEM_OPTS="-Xms1G -Xmx1G -XX:MaxDirectMemorySize=1G"

# GC
GC_LOG_DIR="logs"
mkdir -p ${GC_LOG_DIR}
GC_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseZGC"
GC_OPTS="${GC_OPTS} -Xlog:gc:${GC_LOG_DIR}/gc.log"

# CUSTOM
CUSTOM_OPTS="-Dio.netty.buffer.checkBounds=false"

# CONFIG DIR
#CONF_DIR="conf"
#CUSTOM_OPTS="-Dconf.dir=${CONF_DIR}"

JVM_OPTS="--illegal-access=warn ${MEM_OPTS} ${GC_OPTS}"
if [ "x${CUSTOM_OPTS}" != "x" ]; then
  JVM_OPTS="${JVM_OPTS} ${CUSTOM_OPTS}"
fi

# Spring Boot Arguments
SPRING_PROFILES="prd"

SPRING_ARGS="--spring.profiles.active=${SPRING_PROFILES}"

# Project Version
PRG_VERSION="1.0.0"


# start program
for arg in $*; do
  if [ "$arg" = "--daemon" ]; then
    daemon_mode="true"
  elif [ "$arg" = "-h" ]; then
        echoHelp
        exit 1
  elif [ "$arg" = "--help" ]; then
        echoHelp
        exit 1
  fi
done

if [ "$daemon_mode" == "true" ]; then
  nohup $cmd -server ${JVM_OPTS} -jar entrepot-server-${PRG_VERSION}.jar ${SPRING_ARGS} >/dev/null 2>&1 &
  echo $! > ${PRGDIR}/.pid
else
  exec $cmd -server ${JVM_OPTS} -jar entrepot-server-${PRG_VERSION}.jar ${SPRING_ARGS}
fi
