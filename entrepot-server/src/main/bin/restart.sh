#!/bin/bash


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

pname="carrier-http"

if [ -f ".pid" ] ; then
  pid=$(cat .pid)
  echo "shutdown $pname"
  ./shutdown.sh
  chk=$(ps -p $pid | awk '{print $1}' | grep -w $pid)
  while [ $chk ] ; do
    sleep 1
    chk=$(ps -p $pid | awk '{print $1}' | grep -w $pid)
  done
  echo "startup $pname"
  ./startup.sh --daemon
else
  echo "PID file not found: ${PRGDIR}/.pid"
  exit 1
fi